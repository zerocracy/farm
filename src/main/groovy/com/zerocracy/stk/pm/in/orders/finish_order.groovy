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
package com.zerocracy.stk.pm.in.orders

import com.jcabi.xml.XML
import com.zerocracy.Farm
import com.zerocracy.Par
import com.zerocracy.Policy
import com.zerocracy.Project
import com.zerocracy.cash.Cash
import com.zerocracy.entry.ClaimsOf
import com.zerocracy.farm.Assume
import com.zerocracy.claims.ClaimIn
import com.zerocracy.pm.cost.Boosts
import com.zerocracy.pm.cost.Estimates
import com.zerocracy.pm.cost.Rates
import com.zerocracy.pm.in.Orders
import com.zerocracy.pm.qa.JobAudit
import com.zerocracy.pm.scope.Wbs
import com.zerocracy.pm.staff.Roles
import java.time.Duration
import java.time.Instant
import java.util.concurrent.TimeUnit

def exec(Project project, XML xml) {
  new Assume(project, xml).notPmo()
  new Assume(project, xml).type('Finish order')
  ClaimIn claim = new ClaimIn(xml)
  String job = claim.param('job')
  Orders orders = new Orders(farm, project).bootstrap()
  Instant closed
  if (claim.hasParam('closed')) {
    closed = Instant.parse(claim.param('closed'))
  } else {
    closed = Instant.now()
  }

  long velocity = closed.toEpochMilli() - orders.startTime(job).time
  String performer = orders.performer(job)
  Estimates estimates = new Estimates(farm, project).bootstrap()
  Cash price = Cash.ZERO
  Farm farm = binding.variables.farm
  int speed = 0
  if (velocity <= Duration.ofHours(new Policy().get('36.hours', 48)).toMillis()) {
    // @todo #1381:30min This message has to be moved to the final message in
    //  make_payment.groovy stakeholder. Leaving it here adds a pointless message
    //  in issue tracker, we could reduce it into a single message that contains
    //  all the reasons for bonuses (QA and speed). For that you'll most probably need
    //  to create a new parameter that can be retrieved during the payment.
    claim.copy()
      .type('Notify job')
      .token("job;${job}")
      .param(
      'message',
      new Par('Job was finished in %d hours, bonus for fast delivery is possible (see §36)')
        .say(Duration.ofMillis(velocity).toHours())
    ).postTo(new ClaimsOf(farm, project))
    speed = new Policy().get('36.bonus', 5)
  }
  if (estimates.exists(job)) {
    price = estimates.get(job)
    if (speed > 0 && !price.empty) {
      price = price.add(new Rates(project).bootstrap().rate(performer).mul(speed) / 60)
    }
  }
  int minutes = new Boosts(farm, project).bootstrap().factor(job) * 15 + speed
  Roles roles = new Roles(project).bootstrap()
  List<String> qa = roles.findByRole('QA')
  if (qa.empty || roles.hasRole(performer, 'ARC', 'PO')) {
    List<String> complaints = new JobAudit(farm, project).review(job)
    if (complaints.empty) {
      claim.copy()
        .type('Make payment')
        .param('login', performer)
        .param('reason', new Par('Order was finished').say())
        .param('minutes', minutes)
        .param('cash', price)
        .postTo(new ClaimsOf(farm, project))
    } else {
      claim.copy()
        .type('Notify job')
        .token("job;${job}")
        .param(
          'message',
          new Par('Quality is low, no payment, see §31: %s').say(
            complaints.join(', ')
          )
        )
        .postTo(new ClaimsOf(farm, project))
    }
  } else {
    Cash bonus
    if (new Wbs(project).bootstrap().role(job) == 'REV') {
      bonus = price.mul(new Policy().get('31.rev-bonus', 33)) / 100
    } else {
      bonus = price.mul(new Policy().get('31.bonus', 16)) / 100
    }
    claim.copy()
      .type('Start QA review')
      .param('login', performer)
      .param('minutes', minutes)
      .param('cash', price)
      .param('bonus', bonus)
      .postTo(new ClaimsOf(farm, project))
  }
  orders.resign(job)
  claim.copy()
    .type('Order was finished')
    .param('login', performer)
    .param('age', velocity / TimeUnit.MINUTES.toMillis(1L) as long)
    .postTo(new ClaimsOf(farm, project))
}
