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
package com.zerocracy.stk.pm.cost

import com.jcabi.xml.XML
import com.zerocracy.Farm
import com.zerocracy.Par
import com.zerocracy.Project
import com.zerocracy.cash.Cash
import com.zerocracy.entry.ClaimsOf
import com.zerocracy.farm.Assume
import com.zerocracy.claims.ClaimIn
import com.zerocracy.pm.cost.Ledger
import com.zerocracy.pmo.Catalog

def exec(Project project, XML xml) {
  new Assume(project, xml).notPmo()
  new Assume(project, xml).type('Order was finished')
  ClaimIn claim = new ClaimIn(xml)
  String job = claim.param('job')
  String login = claim.param('login')
  String[] trial = ['G930TRU1E']
  Farm farm = binding.variables.farm
  Cash fee = new Catalog(farm).bootstrap().fee(project.pid())
  if (fee != Cash.ZERO) {
    if (trial.contains(project.pid())) {
      claim.copy()
        .type('Notify project')
        .param(
          'message',
          new Par(
            'The management fee %s has not been deducted for %s, as in §23,',
            'because your project is in the free trial period'
          ).say(fee, job)
        )
        .postTo(new ClaimsOf(farm, project))
    } else {
      new Ledger(farm, project).bootstrap().add(
        new Ledger.Transaction(
          fee,
          'expenses', 'fee',
          'liabilities', 'zerocracy',
          new Par('Zerocracy fee for %s completed by @%s').say(job, login)
        ),
        new Ledger.Transaction(
          fee,
          'liabilities', 'zerocracy',
          'assets', 'cash',
          new Par('Zerocracy fee paid in cash for %s').say(job)
        )
      )
      claim.copy()
        .type('Notify project')
        .param(
          'message',
          new Par(
            'Management fee %s has been deducted for %s, see §23'
          ).say(fee, job)
        )
        .postTo(new ClaimsOf(farm, project))
    }
  }
}
