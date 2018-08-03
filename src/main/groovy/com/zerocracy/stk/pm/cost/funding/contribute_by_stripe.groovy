/*
 * Copyright (c) 2016-2018 Zerocracy
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
import com.zerocracy.entry.ClaimsOf
import com.zerocracy.farm.Assume
import com.zerocracy.claims.ClaimIn
import com.zerocracy.pm.cost.Ledger

/**
 * This stakeholder process user Stripe contributions: it adds funds
 * to the project and notify project in Slack about contribution.
 * Everyone can donate to free and public open-source projects
 * (see {@link com.zerocracy.tk.project.TkContrib}
 * via 'Contribute badges' ({@link com.zerocracy.tk.project.TkContribBadge}).
 * @param project Contributed project
 * @param xml Claim
 */
def exec(Project project, XML xml) {
  new Assume(project, xml).notPmo()
  new Assume(project, xml).type('Contributed by Stripe')
  ClaimIn claim = new ClaimIn(xml)
  Cash amount = new Cash.S(claim.param('amount'))
  Farm farm = binding.variables.farm
  new Ledger(project).bootstrap().add(
    new Ledger.Transaction(
      amount,
      'assets', 'cash',
      'income', "@${claim.author()}",
      new Par('Contributed via Stripe by %s').say(claim.author())
    )
  )
  claim.copy()
    .type('Notify project')
    .param(
      'message',
      new Par(
        'The project %s has been funded via Stripe for %s;',
        'it was a free contribution of @%s, as in §50'
      ).say(project.pid(), amount, claim.author())
    )
    .postTo(new ClaimsOf(farm, project))
  claim.copy().type('Notify PMO').param(
    'message', new Par(
      'We just funded %s for %s via Stripe by @%s'
    ).say(project.pid(), amount, claim.author())
  ).postTo(new ClaimsOf(farm, project))
  claim.copy().type('Tweet').param(
    'par', new Par(
      farm,
      'The project %s received a monetary contribution of %s',
      'from https://github.com/%s; many thanks for your support!'
    ).say(project.pid(), amount, claim.author())
  ).postTo(new ClaimsOf(farm, project))
  claim.copy().type('Send zold')
    .param('recipient', claim.author())
    .param('reason', 'contribution reward')
    .postTo(new ClaimsOf(farm, project))
}
