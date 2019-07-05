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

package com.zerocracy.stk.pm.in.orders

import com.jcabi.xml.XML
import com.zerocracy.Farm
import com.zerocracy.Par
import com.zerocracy.Policy
import com.zerocracy.Project
import com.zerocracy.entry.ClaimsOf
import com.zerocracy.farm.Assume
import com.zerocracy.claims.ClaimIn
import com.zerocracy.pm.PktOptions
import com.zerocracy.pm.cost.Boosts
import com.zerocracy.pm.cost.Ledger
import com.zerocracy.pm.in.Impediments
import com.zerocracy.pm.in.JobExpired
import com.zerocracy.pm.in.Orders
import com.zerocracy.pm.scope.Wbs
import com.zerocracy.pm.staff.Roles
import com.zerocracy.pmo.Pmo
import java.time.ZoneOffset
import java.time.ZonedDateTime
import org.cactoos.iterable.Filtered
import org.cactoos.iterable.Limited

/**
 * Resign an order that is kept for too long.
 * Orders in unfunded projects are not resigned at all, similarly REV tasks
 * are not resigned even in funded projects.
 *
 * @param project A project
 * @param xml XML file received
 */
def exec(Project project, XML xml) {
  new Assume(project, xml).notPmo()
  new Assume(project, xml).type('Ping')
  Farm farm = binding.variables.farm
  if (new Ledger(farm, project).bootstrap().cash().decimal() <= BigDecimal.ZERO) {
    // We must not resign when the project is not funded, simply
    // because developers can't do anything without money. Their PRs
    // will have no reviewers, etc.
    return
  }
  ClaimIn claim = new ClaimIn(xml)
  ZonedDateTime time = ZonedDateTime.ofInstant(
    claim.created().toInstant(), ZoneOffset.UTC
  )
  Orders orders = new Orders(farm, project).bootstrap()
  Boosts boosts = new Boosts(farm, project).bootstrap()
  Impediments impediments = new Impediments(farm, project).bootstrap()
  Roles pmos = new Roles(new Pmo(farm)).bootstrap()
  List<String> waiting = impediments.jobs().toList()
  Policy policy = new Policy()
  Wbs wbs = new Wbs(project).bootstrap()
  int days = new PktOptions(project, farm).bootstrap().daysToCloseTask()
  new Limited<>(
    5,
    new Filtered<String>(
      { job ->
        !waiting.contains(job) &&
          new JobExpired(new Pmo(farm), orders, policy, time.toLocalDateTime(), job).value()
      },
      orders.olderThan(time.minusDays(days))
    )
  ).forEach { String job ->
    String worker = orders.performer(job)
    if (wbs.role(job) == 'REV') {
      // We are not removing REV performers, because they can't responsible
      // for the delays there, since the code is not theirs.
      return
    }
    if (pmos.hasAnyRole(worker)) {
      // Members of PMO have special status, we should not resign
      // them from any tasks ever.
      return
    }
    if (impediments.exists(job)) {
      // We must not resign if the job has an impediment, this is what
      // impediments are about: they prevent automatic resignation
      return
    }
    claim.copy()
      .type('Cancel order')
      .token("job;$job")
      .param('job', job)
      .param('reason', new Par('It is older than %d day(s), see §8').say(days))
      .postTo(new ClaimsOf(farm, project))
    claim.copy()
      .type('Add award points')
      .param('job', job)
      .param('login', worker)
      .param('reason', new Par('Resigned on delay, see §8').say())
      .param('minutes', boosts.factor(job) * -15)
      .postTo(new ClaimsOf(farm, project))
    claim.copy()
      .type('Add negligence')
      .param('job', job)
      .param('login', worker)
      .postTo(new ClaimsOf(farm, new Pmo(farm)))
    claim.copy()
      .type('Notify project')
      .param(
      'message',
        new Par(
          'The order at %s cancelled for @%s, it is over %d day(s), see §8'
        ).say(job, worker, days)
      )
      .postTo(new ClaimsOf(farm, project))
  }
}
