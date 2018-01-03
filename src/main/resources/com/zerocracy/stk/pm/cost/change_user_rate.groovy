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
package com.zerocracy.stk.pm.cost

import com.jcabi.xml.XML
import com.zerocracy.farm.Assume
import com.zerocracy.jstk.Farm
import com.zerocracy.jstk.Project
import com.zerocracy.jstk.SoftException
import com.zerocracy.jstk.cash.Cash
import com.zerocracy.pm.ClaimIn
import com.zerocracy.pm.ClaimOut
import com.zerocracy.pm.cost.Rates
import com.zerocracy.pmo.People

def exec(Project project, XML xml) {
  new Assume(project, xml).notPmo()
  new Assume(project, xml).type('Change user rate')
  new Assume(project, xml).roles('ARC', 'PO')
  ClaimIn claim = new ClaimIn(xml)
  String login = claim.param('login')
  Farm farm = binding.variables.farm
  People people = new People(farm)
  if (people.wallet(login).empty) {
    throw new SoftException(
      "User @${login} doesn't have a payment method configured yet, we won't be able to pay them."
    )
  }
  Cash rate = new Cash.S(claim.param('rate'))
  Rates rates = new Rates(project).bootstrap()
  String msg
  if (rates.exists(login)) {
    msg = "Hourly rate of @${login} changed from ${rates.rate(login)} to ${rate}."
  } else {
    msg = "Hourly rate of @${login} set to ${rate}."
  }
  rates.set(login, rate)
  new ClaimOut()
    .type('User rate was changed')
    .param('login', login)
    .param('rate', rate)
    .postTo(project)
  claim.reply(msg).postTo(project)
}
