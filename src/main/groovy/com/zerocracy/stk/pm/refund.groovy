/*
 * Copyright (c) 2016-2019 Zerocracy
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to read
 * the Software only. Permissions is hereby NOT GRANTED to use, copy, modify,
 * merge, publish, distribute, sublicense, and/or sell copies of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.zerocracy.stk.pm

import com.jcabi.xml.XML
import com.zerocracy.Farm
import com.zerocracy.Par
import com.zerocracy.Project
import com.zerocracy.SoftException
import com.zerocracy.cash.Cash
import com.zerocracy.claims.ClaimIn
import com.zerocracy.entry.ClaimsOf
import com.zerocracy.farm.Assume
import com.zerocracy.pm.cost.Estimates
import com.zerocracy.pm.cost.Ledger

/**
 * Refund the project.
 * <p>
 *   This stakeholder adds new transaction from assets:cash
 *   to payables:refund with specified amount and notify PMO about refund.
 * </p>
 *
 * @param project Project
 * @param xml Claim with amount param
 */
def exec(Project project, XML xml) {
  new Assume(project, xml).notPmo().type('Refund').roles('PO')
  ClaimIn claim = new ClaimIn(xml)
  Cash amount = new Cash.S(claim.param('amount'))
  Farm farm = binding.variables.farm
  Ledger ledger = new Ledger(farm, project).bootstrap()
  Estimates estimates = new Estimates(farm, project)
  Cash cash = ledger.cash()
  Cash locked = estimates.total()
  Cash available = cash.add(locked.mul(-1L))
  if (amount > available) {
    throw new SoftException(
      new Par(
        farm,
        'We can\'t refund for %s because you don\'t have enough cash',
        'or it was estimated and locked.',
        'cash/estimated/available: %s/%s/%s',
      ).say(amount, cash, locked, available)
    )
  }
  String login = claim.author()
  ledger.add(
    new Ledger.Transaction(
      amount,
      'expenses', 'payables',
      'assets', 'cash',
      "refund by $login"
    )
  )
  claim.reply(
    new Par(
      farm,
      'Project was refunded for %s',
      'Cash (est): %s (%s)'
    ).say(amount, ledger.cash(), locked)
  ).postTo(new ClaimsOf(farm, project))
  claim.copy().type('Notify PMO').param(
    'message',
    new Par(farm, 'User @%s requested refund for %s in %s')
      .say(login, amount, project.pid())
  ).postTo(new ClaimsOf(farm))
}
