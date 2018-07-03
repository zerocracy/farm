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
package com.zerocracy.stk.pm.cost.funding

import com.jcabi.xml.XML
import com.zerocracy.Farm
import com.zerocracy.Par
import com.zerocracy.Project
import com.zerocracy.cash.Cash
import com.zerocracy.farm.Assume
import com.zerocracy.farm.props.Props
import com.zerocracy.pm.ClaimIn
import com.zerocracy.pmo.banks.Zold
import java.util.regex.Pattern

def exec(Project project, XML xml) {
  // @todo #1291:30min Add tests for payments via concrete banks, I suppose
  //  `Payroll` may check 'testing' property in farm
  //  and return fake `Bank` implementations for each supported payment
  //  method. First of all we need to test Zold payments.
  new Assume(project, xml).notPmo()
  new Assume(project, xml).type('Send zold')
  Farm farm = binding.variables.farm
  if (new Props(farm).has('//testing')) {
    return
  }
  ClaimIn claim = new ClaimIn(xml)
  Cash amount = new Cash.S(claim.param('amount'))
  String recipient = claim.param('recipient')
  String reason = claim.param('reason')
  new Zold(farm).pay(
    recipient,
    amount,
    reason.replaceAll(Pattern.compile("[^a-zA-Z0-9 @!?*_\\-.:,']+"), ' ')
  )
  claim.copy().type('Notify user')
    .token("user;${recipient}")
    .param(
    'message',
    new Par('We just sent you %s ZLD through https://wts.zold.io')
      .say(amount.decimal())
  )
  claim.copy().type('Notify PMO').param(
    'message',
    new Par(
      'We just sent %s ZLD to %s as %s via wts.zold.io in %s'
    ).say(amount, recipient, reason, project.pid())
  ).postTo(project)
}
