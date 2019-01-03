/*
 * Copyright (c) 2016-2019 Zerocracy
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
import com.zerocracy.Policy
import com.zerocracy.Project
import com.zerocracy.cash.Cash
import com.zerocracy.claims.ClaimIn
import com.zerocracy.entry.ClaimsOf
import com.zerocracy.farm.Assume
import com.zerocracy.pm.cost.Estimates
import com.zerocracy.pm.cost.Ledger
import com.zerocracy.pmo.Debts
import com.zerocracy.pmo.People
import com.zerocracy.pmo.banks.Payroll

/**
 * Make cash payment.
 * <p>
 * Send cash to user's wallet via available bank account.
 *
 * @param project Project
 * @param xml Claim
 */
def exec(Project project, XML xml) {
  new Assume(project, xml).type('Pay cash')
  Farm farm = binding.variables.farm
  ClaimIn claim = new ClaimIn(xml)
  String job = claim.param('job')
  String login = claim.param('login')
  String reason = new Par.ToText(claim.param('reason')).toString()
  if (!claim.hasParam('cash')) {
    return
  }
  Cash price = new Cash.S(claim.param('cash'))
  if (price.empty) {
    return
  }
  Ledger ledger = new Ledger(farm, project).bootstrap()
  if (!canPay(farm, project, job, price)) {
    claim.reply('The project doesn\'t have enough funds, can\'t make a payment')
      .postTo(new ClaimsOf(farm, project))
  }
  String tail = ''
  People people = new People(farm).bootstrap()
  boolean hasMentor = people.hasMentor(login) && people.mentor(login) != '0crat'
  if (!claim.hasParam('no-tuition-fee') && hasMentor && project.pid() != 'PMO') {
    int share = new Policy().get('45.share', 8)
    Cash fee = price.mul(share) / 100
    price = price.mul(100 - share) / 100
    String mentor = people.mentor(login)
    claim.copy()
      .type('Make payment')
      .param('login', mentor)
      .param('minutes', 0)
      .param('cash', fee)
      .param('student', login)
      .param('no-tuition-fee', true)
      .param('reason', new Par('Tuition fee from @%s').say(login))
      .postTo(new ClaimsOf(farm, project))
    tail = new Par(
      'the tuition fee %s was deducted',
      'and sent to @%s (your mentor), according to §45'
    ).say(fee, mentor)
  }
  String msg
  try {
    msg = new Payroll(farm).pay(
      ledger,
      login, price, "Payment for ${job}: ${reason}",
      job
    )
    claim.copy()
      .type('Payment was made')
      .param('amount', price)
      .param('payment_id', msg)
      .postTo(new ClaimsOf(farm, project))
    claim.copy()
      .type('Notify user')
      .token("user;${login}")
      .param(
        'message',
        new Par(
          'We just paid you %s (`%s`) for %s: %s'
        ).say(price, msg, job, reason) + tail
      )
      .postTo(new ClaimsOf(farm, project))
  } catch (IOException ex) {
    new Debts(farm).bootstrap().add(login, price, "${reason} at ${job}", ex.message)
    Cash commission = price.mul(3) / 100
    ledger.add(
      new Ledger.Transaction(
        price.add(commission),
        'liabilities', 'debt',
        'assets', 'cash',
        reason + new Par(' (amount:%s, commission:%s)').say(price, commission)
      ),
      new Ledger.Transaction(
        commission,
        'expenses', 'jobs',
        'liabilities', 'debt',
        "${commission} (commission)"
      ),
      new Ledger.Transaction(
        price,
        'expenses', 'jobs',
        'liabilities', "@${login}",
        reason
      )
    )
    claim.copy()
      .type('Payment was added to debts')
      .param('amount', price)
      .postTo(new ClaimsOf(farm, project))
    claim.copy()
      .type('Notify user')
      .token("user;${login}")
      .param(
        'message',
        new Par(
          'We are very sorry, but we failed to pay you %s for %s: "%s";',
          'this amount was added to the list of payments we owe you;',
          'we will try to send them all together very soon;',
          'we will keep you informed, see §20'
        ).say(price, job, ex.message) + tail
      )
      .postTo(new ClaimsOf(farm, project))
  }
  claim.copy()
    .type('Notify project')
    .param(
      'message',
      new Par(
        'We just paid %s to @%s for %s: %s'
      ).say(price, login, job, reason)
    )
    .postTo(new ClaimsOf(farm, project))
}


static canPay(Farm farm, Project project, String job, Cash price) {
  if (project.pid() == 'PMO') {
    return true
  }
  Estimates estimates = new Estimates(farm, project).bootstrap()
  if (estimates.exists(job)) {
    return true
  }
  Ledger ledger = new Ledger(farm, project).bootstrap()
  !ledger.deficit() && ledger.cash() > estimates.total().add(price)
}