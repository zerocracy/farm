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
package com.zerocracy.stk.pm.cost.funding

import com.jcabi.xml.XML
import com.zerocracy.Farm
import com.zerocracy.Par
import com.zerocracy.Project
import com.zerocracy.cash.Cash
import com.zerocracy.claims.ClaimIn
import com.zerocracy.claims.ClaimOut
import com.zerocracy.entry.ClaimsOf
import com.zerocracy.farm.Assume
import com.zerocracy.pm.cost.Ledger
import com.zerocracy.pmo.Catalog
import com.zerocracy.pmo.recharge.Recharge

/**
 * This stakeholder is called when project is funded by Zold,
 * PO can fund it directly from project page in
 * {@link com.zerocracy.tk.project.TkFundZold}.
 *
 * @param project Funded project
 * @param xml Claim
 *
 */
def exec(Project project, XML xml) {
  new Assume(project, xml).notPmo()
  new Assume(project, xml).type('Funded by Zold')
  Farm farm = binding.variables.farm
  ClaimIn claim = new ClaimIn(xml)
  Cash amount = new Cash.S(claim.param('amount'))
  String cid = claim.param('callback')
  String txn = claim.param('txn')
  new Ledger(farm, project).bootstrap().add(
    new Ledger.Transaction(
      amount,
      'assets', 'cash',
      'income', cid,
      "Funded via Zold, txn=${txn}"
    )
  )
  claim.copy()
    .type('Notify project')
    .param(
      'message',
      new Par(
        farm,
        'The project %s has been funded via Zold for %s;',
        'transaction is `%s`, callback id is `%s`'
      ).say(project.pid(), amount, txn, cid)
    ).postTo(new ClaimsOf(farm, project))
  claim.copy().type('Notify PMO').param(
    'message', new Par(
      farm,
      'We just funded %s for %s via Zold'
    ).say(project.pid(), amount)
  ).postTo(new ClaimsOf(farm, project))
  Catalog catalog = new Catalog(farm).bootstrap()
  Recharge recharge = new Recharge(farm, project)
  if (recharge.exists()) {
    recharge.delete()
  }
  if (catalog.hasAdviser(project.pid())) {
    Cash bonus = amount.mul(4) / 100
    String adviser = catalog.adviser(project.pid())
    new ClaimOut()
      .type('Make payment')
      .param('login', adviser)
      .param('job', 'none')
      .param('cash', bonus)
      .param(
      'reason',
        new Par(farm, 'Adviser payment for @%s project (%s)')
          .say(project.pid(), txn)
      ).postTo(new ClaimsOf(farm))
    new ClaimOut().type('Notify PMO').param(
      'message',
      new Par(farm, 'We just send adviser payment of %s for %s to %s').say(bonus, project.pid(), adviser)
    ).postTo(new ClaimsOf(farm))
  }
}
