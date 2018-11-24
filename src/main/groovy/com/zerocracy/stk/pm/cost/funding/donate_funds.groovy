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
 * Internal mechanism to add some amount of funds
 * to a project without actual funding. This stakeholder
 * can be called only by 'yegor256' (see project.xsl).
 *
 * @param project Project to fund
 * @param xml Claim
 */
def exec(Project project, XML xml) {
  new Assume(project, xml).notPmo()
  new Assume(project, xml).type('Donate')
  ClaimIn claim = new ClaimIn(xml)
  String author = claim.author()
  Cash amount = new Cash.S(claim.param('amount'))
  new Ledger(farm, project).bootstrap().add(
    new Ledger.Transaction(
      amount,
      'assets', 'cash',
      'income', 'zerocracy',
      new Par('Donated by @%s').say(author)
    )
  )
  Farm farm = binding.variables.farm
  claim.copy()
    .type('Notify project')
    .param(
      'message',
      new Par(
        'The project %s got a donation of %s from @%s'
      ).say(project.pid(), amount, author)
    )
    .postTo(new ClaimsOf(farm, project))
}
