/**
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
package com.zerocracy.stk.pmo.debts

import com.jcabi.xml.XML
import com.jcabi.xml.XMLDocument
import com.zerocracy.Farm
import com.zerocracy.Par
import com.zerocracy.Policy
import com.zerocracy.Project
import com.zerocracy.cash.Cash
import com.zerocracy.farm.Assume
import com.zerocracy.farm.fake.FkProject
import com.zerocracy.pm.ClaimIn
import com.zerocracy.pm.cost.Ledger
import com.zerocracy.pmo.Debts
import com.zerocracy.pmo.banks.Payroll
import org.xembly.Xembler

def exec(Project pmo, XML xml) {
  new Assume(pmo, xml).isPmo()
  new Assume(pmo, xml).type('Ping hourly')
  Debts debts = new Debts(pmo).bootstrap()
  ClaimIn claim = new ClaimIn(xml)
  Farm farm = binding.variables.farm
  debts.iterate().each { uid ->
    if (!debts.expired(uid)) {
      return
    }
    Cash debt = debts.amount(uid)
    if (debt < new Policy().get('46.threshold', new Cash.S('$50'))) {
      return
    }
    try {
      String details = new XMLDocument(new Xembler(debts.toXembly(uid)).xmlQuietly()).xpath(
        '//item/amount/text()'
      ).join(', ')
      String pid = new Payroll(farm).pay(
        new Ledger(new FkProject()).bootstrap(),
        uid, debt,
        new Par('Debt repayment, per §46: %s').say(details)
      )
      debts.remove(uid)
      claim.copy()
        .type('Debt was paid')
        .param('login', uid)
        .param('payment_id', pid)
        .param('details', details)
        .param('amount', debt)
        .postTo(pmo)
      claim.copy()
        .type('Notify user')
        .token("user;${uid}")
        .param(
          'message',
          new Par(
            'We just paid you the debt of %s (`%s`)'
          ).say(debt, pid)
        )
        .postTo(pmo)
    } catch (IOException ex) {
      debts.failure(uid, ex.message)
      claim.copy()
        .type('Debt payment failed')
        .param('login', uid)
        .param('amount', debt)
        .param('failure', ex.message)
        .postTo(pmo)
      claim.copy()
        .type('Notify user')
        .token("user;${uid}")
        .param(
          'message',
          new Par(
            'We tried to pay your debt of %s, but failed (%s);',
            'don\'t worry, we will retry very soon'
          ).say(debt, ex.message)
        )
        .postTo(pmo)
      claim.copy().type('Notify PMO').param(
        'message',
        new Par(
          'We failed to pay the debt of %s to @%s: %s'
        ).say(debt, uid, ex.message)
      ).postTo(pmo)
    }
  }
}
