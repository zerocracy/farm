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
package com.zerocracy.stk.pm.in.orders

import com.jcabi.xml.XML
import com.zerocracy.Farm
import com.zerocracy.Par
import com.zerocracy.Policy
import com.zerocracy.Project
import com.zerocracy.cash.Cash
import com.zerocracy.farm.Assume
import com.zerocracy.pm.ClaimIn
import com.zerocracy.pm.cost.Boosts
import com.zerocracy.pm.cost.Estimates
import com.zerocracy.pm.cost.Rates
import com.zerocracy.pm.in.Orders
import com.zerocracy.pm.qa.JobAudit
import com.zerocracy.pm.staff.Roles
import java.util.concurrent.TimeUnit

def exec(Project project, XML xml) {
  new Assume(project, xml).notPmo()
  new Assume(project, xml).type('Finish order')
  ClaimIn claim = new ClaimIn(xml)
  String job = claim.param('job')
  Orders orders = new Orders(project).bootstrap()
  long age = (System.currentTimeMillis() - orders.startTime(job).time) / TimeUnit.MINUTES.toMillis(1L)
  String performer = orders.performer(job)
  Estimates estimates = new Estimates(project).bootstrap()
  Cash price = Cash.ZERO
  if (estimates.exists(job)) {
    price = estimates.get(job)
  }
  int minutes = new Boosts(project).bootstrap().factor(job) * 15
  Roles roles = new Roles(project).bootstrap()
  List<String> qa = roles.findByRole('QA')
  if (qa.empty || roles.hasRole(performer, 'ARC', 'PO')) {
    Farm farm = binding.variables.farm
    List<String> complaints = new JobAudit(farm, project).review(job)
    if (complaints.empty) {
      claim.copy()
        .type('Make payment')
        .param('login', performer)
        .param('reason', new Par('Order was finished').say())
        .param('minutes', minutes)
        .param('cash', price)
        .postTo(project)
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
        .postTo(project)
    }
  } else {
    Rates rates = new Rates(project).bootstrap()
    Cash bonus = Cash.ZERO
    if (rates.exists(performer)) {
      bonus = rates.rate(performer).mul(new Policy().get('31.bonus', 5)) / 60
    }
    claim.copy()
      .type('Start QA review')
      .param('login', performer)
      .param('minutes', minutes)
      .param('cash', price)
      .param('bonus', bonus)
      .postTo(project)
  }
  orders.resign(job)
  claim.copy()
    .type('Order was finished')
    .param('login', performer)
    .param('age', age)
    .postTo(project)
}
