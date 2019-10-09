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
import com.zerocracy.Project
import com.zerocracy.SoftException
import com.zerocracy.cash.Cash
import com.zerocracy.claims.ClaimIn
import com.zerocracy.claims.ClaimOut
import com.zerocracy.entry.ClaimsOf
import com.zerocracy.farm.Assume
import com.zerocracy.pm.cost.Estimates
import com.zerocracy.pm.cost.Ledger
import com.zerocracy.pm.cost.Rates
import com.zerocracy.pmo.recharge.Recharge

import java.time.Duration

/**
 * Make payment is payment dispatcher stakeholder.
 * <p>
 * It parses origin claim and redirect it to 'Add award points',
 * 'Pay cash', 'Transfer shares' and 'Send zold'.
 *
 * @param project Project or PMO
 * @param xml Claim
 */
def exec(Project project, XML xml) {
  new Assume(project, xml).type('Make payment')
  Farm farm = binding.variables.farm
  ClaimIn claim = new ClaimIn(xml)
  // @todo #1162:30min In continuation of #1162, add a test to ensure that a payment
  //  results on points being awarded. Also add tests for transfer_shares and pay_cash stakeholders.
  claim.copy()
    .type('Add award points')
    .postTo(new ClaimsOf(farm, project))
  String login = claim.param('login')
  int minutes
  if (claim.hasParam('minutes')) {
    minutes = Integer.parseInt(claim.param('minutes'))
  } else {
    minutes = 0
  }
  if (minutes < 0) {
    return
  }
  Cash price
  if (claim.hasParam('cash')) {
    price = new Cash.S(claim.param('cash'))
  } else {
    Cash rate = Cash.ZERO
    Rates rates = new Rates(project).bootstrap()
    if (rates.exists(login)) {
      rate = rates.rate(login)
    }
    price = rate.mul(minutes) / 60
  }
  boolean estimated = Boolean.parseBoolean(claim.param('estimated', Boolean.FALSE.toString()))
  int attempt = Integer.parseInt(claim.param('attempt', '0'))
  if (attempt > 7) {
    throw new SoftException(
      new Par(
        'We failed to pay in %d attempts'
      ).say(attempt)
    )
  }
  boolean canRecharge = new Recharge(farm, project).exists()
  if (canRecharge && !canPay(farm, project, estimated, price)) {
      new ClaimOut()
        .type('Recharge project')
        .param('triggered_by', new ClaimIn(xml).cid())
        .param('force', true)
        .unique('recharge')
        .postTo(new ClaimsOf(farm, project))
      ClaimOut copy = claim.copy(false)
        .param('attempt', attempt + 1)
        .until(Duration.ofMinutes(10))
      if (claim.hasAuthor()) {
        copy = copy.author(claim.author())
      }
      if (claim.hasToken()) {
        copy = copy.token(claim.token())
      }
      copy.postTo(new ClaimsOf(farm, project))
    throw new SoftException(
      new Par(
        'The project is under-funded, you can\'t do it now, see §49',
        'We just triggered recharge and will retry to make payment in 10 minutes (%d/8).'
      ).say(attempt)
    )
  }
  Cash cash = price
  if (!canPay(farm, project, estimated, price)) {
    cash = Cash.ZERO
  }
  if (!cash.empty) {
    claim.copy()
      .type('Pay cash')
      .param('cash', cash)
      .postTo(new ClaimsOf(farm, project))
  }
  if (!claim.hasParam('student') && claim.hasParam('job')) {
    claim.copy()
      .type('Transfer shares')
      .param('cash', cash)
      .postTo(new ClaimsOf(farm, project))
  }
}

static canPay(Farm farm, Project project, boolean est, Cash price) {
  if (price.empty) {
    return true
  }
  if (project.pid() == 'PMO') {
    return true
  }
  if (est) {
    return true
  }
  Estimates estimates = new Estimates(farm, project).bootstrap()
  Ledger ledger = new Ledger(farm, project).bootstrap()
  !ledger.deficit() && ledger.cash() > estimates.total().add(price)
}