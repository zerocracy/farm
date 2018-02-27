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
package com.zerocracy.stk.pmo.people

import com.jcabi.xml.XML
import com.jcabi.xml.XMLDocument
import com.zerocracy.Par
import com.zerocracy.Policy
import com.zerocracy.Project
import com.zerocracy.cash.Cash
import com.zerocracy.farm.Assume
import com.zerocracy.pm.ClaimIn
import com.zerocracy.pmo.Debts
import org.xembly.Xembler

def exec(Project pmo, XML xml) {
  new Assume(pmo, xml).isPmo()
  new Assume(pmo, xml).type('Ping daily')
  Debts debts = new Debts(pmo).bootstrap()
  ClaimIn claim = new ClaimIn(xml)
  debts.iterate().each { uid ->
    Cash debt = debts.amount(uid)
    if (debt < new Policy().get('46.threshold', new Cash.S('$50'))) {
      return
    }
    claim.copy()
      .type('Make payment')
      .param('cash', debt)
      .param('no-tuition-fee', true)
      .param('minutes', 0)
      .param('login', uid)
      .param(
        'message',
        new Par('Debt repayment, per §20: %s').say(
          new XMLDocument(new Xembler(debts.toXembly(uid)).xmlQuietly()).xpath(
            '//item/amount/text()'
          ).join(', ')
        )
      )
      .postTo(pmo)
    debts.remove(uid)
  }
}
