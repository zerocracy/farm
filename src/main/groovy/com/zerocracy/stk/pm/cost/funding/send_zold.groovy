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
import com.zerocracy.farm.props.Props
import com.zerocracy.claims.ClaimIn
import com.zerocracy.pm.cost.Vesting
import com.zerocracy.pmo.banks.Zold

/**
 * Send Zold to user via wts.zold.io API and notify user about payment.
 *
 * @param project Current project
 * @param xml Claim
 */
def exec(Project project, XML xml) {
  // @todo #1291:30min Add tests for payments via concrete banks, I suppose
  //  `Payroll` may check 'testing' property in farm
  //  and return fake `Bank` implementations for each supported payment
  //  method. First of all we need to test Zold payments.
  new Assume(project, xml).notPmo()
  new Assume(project, xml).type('Send zold')
  Farm farm = binding.variables.farm
  ClaimIn claim = new ClaimIn(xml)
  String recipient = claim.param('recipient')
  Vesting vesting = new Vesting(project).bootstrap()
  Cash amount = new Cash.S(claim.param('amount'))
  if (claim.hasParam('minutes') && vesting.exists(recipient)){
    amount = vesting.rate(recipient).mul(Integer.parseInt(claim.param('minutes'))) / 60
  }
  String reason = claim.param('reason')
  // @todo #1119:30min Reason is not attached to transaction as 'details'
  //  because of this bug: https://github.com/zold-io/wts.zold.io/issues/36
  //  let's use reason claim parameter as reason when it will be fixed.
  if (!new Props(farm).has('//testing')) {
    new Zold(farm).pay(
      recipient,
      amount,
      'none'
    )
  }
  claim.copy().type('Notify user')
    .token("user;${recipient}")
    .param(
    'message',
    new Par('We just sent you %s ZLD through https://wts.zold.io')
      .say(amount.decimal())
  ).postTo(new ClaimsOf(farm, project))
  claim.copy().type('Notify PMO').param(
    'message',
    new Par(
      'We just sent %s ZLD to %s as %s via wts.zold.io in %s'
    ).say(amount, recipient, reason, project.pid())
  ).postTo(new ClaimsOf(farm, project))
}
