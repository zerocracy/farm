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
package com.zerocracy.stk.pm.cost.rates

import com.jcabi.xml.XML
import com.zerocracy.Farm
import com.zerocracy.Par
import com.zerocracy.Project
import com.zerocracy.SoftException
import com.zerocracy.cash.Cash
import com.zerocracy.entry.ClaimsOf
import com.zerocracy.farm.Assume
import com.zerocracy.claims.ClaimIn
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
  Cash rate = new Cash.S(claim.param('rate'))
  if (people.wallet(login).empty && rate != Cash.ZERO) {
    throw new SoftException(
      new Par(
        '@%s doesn\'t have a payment method configured yet,',
        'we won\'t be able to pay them.',
        'That\'s why the rate %s can\'t be set.'
      ).say(login, rate)
    )
  }
  Rates rates = new Rates(project).bootstrap()
  String msg
  if (rates.exists(login)) {
    Cash before = rates.rate(login)
    if (before == rate) {
      throw new SoftException(
        new Par(
          'Hourly rate of @%s remains %s, no need to change'
        ).say(login, rate)
      )
    }
    msg = new Par(
      'Hourly rate of @%s changed from %s to %s'
    ).say(login, before, rate)
  } else {
    if (rate == Cash.ZERO) {
      throw new SoftException(
        new Par(
          'Hourly rate of @%s remains zero'
        ).say(login)
      )
    }
    msg = new Par(
      'Hourly rate of @%s was changed from zero to %s'
    ).say(login, rate)
  }
  rates.set(login, rate)
  claim.copy()
    .type('User rate was changed')
    .param('login', login)
    .param('rate', rate)
    .postTo(new ClaimsOf(farm, project))
  claim.reply(msg).postTo(new ClaimsOf(farm, project))
}
