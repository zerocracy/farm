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
package com.zerocracy.stk.pm.cost.vesting

import com.jcabi.xml.XML
import com.zerocracy.Farm
import com.zerocracy.Par
import com.zerocracy.Project
import com.zerocracy.SoftException
import com.zerocracy.cash.Cash
import com.zerocracy.entry.ClaimsOf
import com.zerocracy.farm.Assume
import com.zerocracy.claims.ClaimIn
import com.zerocracy.pm.cost.Vesting

def exec(Project project, XML xml) {
  new Assume(project, xml).notPmo()
  new Assume(project, xml).type('Change user vesting rate')
  new Assume(project, xml).roles('ARC', 'PO')
  ClaimIn claim = new ClaimIn(xml)
  String login = claim.param('login')
  Cash rate = new Cash.S(claim.param('rate'))
  Vesting vesting = new Vesting(project).bootstrap()
  String msg
  if (vesting.exists(login)) {
    if (vesting.rate(login) == rate) {
      throw new SoftException(
        new Par(
          'Vesting rate for @%s is %s, no need to change'
        ).say(login, rate)
      )
    }
    msg = new Par(
      'Vesting rate for @%s was changed from %s to %s, according to §37'
    ).say(login, vesting.rate(login), rate)
  } else {
    msg = new Par(
      'Vesting rate for @%s was set to %s, according to §37'
    ).say(login, rate)
  }
  vesting.rate(login, rate)
  Farm farm = binding.variables.farm
  claim.reply(msg).postTo(new ClaimsOf(farm, project))
  claim.copy().type('User vesting rate was changed').postTo(new ClaimsOf(farm, project))
}
