package com.zerocracy.stk.pm.in.orders

import com.jcabi.xml.XML
import com.zerocracy.*
import com.zerocracy.farm.Assume
import com.zerocracy.pm.ClaimIn
import com.zerocracy.pm.cost.Boosts
import com.zerocracy.pm.cost.Ledger
import com.zerocracy.pm.in.Impediments
import com.zerocracy.pm.in.Orders
import com.zerocracy.pm.staff.Roles
import com.zerocracy.pm.time.Reminders
import com.zerocracy.pmo.Pmo
import org.cactoos.iterable.Filtered
import org.cactoos.iterable.Limited

import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime

def exec(Project project, XML xml) {
  new Assume(project, xml).notPmo()
  new Assume(project, xml).type('Ping')
  if (new Ledger(project).bootstrap().deficit()) {
    // We must not resign when the project is not funded, simply
    // because developers can't do anything without money. Their PRs
    // will have no reviewers, etc.
    return
  }
  ClaimIn claim = new ClaimIn(xml)
  ZonedDateTime time = ZonedDateTime.ofInstant(
    claim.created().toInstant(), ZoneOffset.UTC
  )
  Orders orders = new Orders(project).bootstrap()
  Boosts boosts = new Boosts(project).bootstrap()
  Impediments impediments = new Impediments(project).bootstrap()
  Farm farm = binding.variables.farm
  Roles pmos = new Roles(new Pmo(farm)).bootstrap()
  Reminders reminders = new Reminders(project).bootstrap()
  List<String> waiting = impediments.jobs().toList()
  JobSchedule schedule = new JobSchedule(farm, orders)
  int days = new Policy().get('8.days', 10)
  LocalDateTime now = LocalDateTime.now()
  new Limited<>(
    5,
    new Filtered<String>(
      { job -> !waiting.contains(job) && schedule.resign(job).isBefore(now) },
      orders.olderThan(time.minusDays(days))
    )
  ).forEach { String job ->
    String worker = orders.performer(job)
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
    if (reminders.labels(job).empty) {
      // We must not resign if we haven't notified the performer
      // at least once
      return
    }
    claim.copy()
      .type('Cancel order')
      .token("job;$job")
      .param('job', job)
      .param('reason', new Par('It is older than %d day(s), see §8').say(days))
      .postTo(project)
    claim.copy()
      .type('Make payment')
      .param('job', job)
      .param('login', worker)
      .param('reason', new Par('Resigned on delay, see §8').say())
      .param('minutes', boosts.factor(job) * -15)
      .postTo(project)
    claim.copy()
      .type('Notify project')
      .param(
        'message',
        new Par(
          'The order at %s cancelled for @%s, it is over %d day(s), see §8'
        ).say(job, worker, days)
      )
      .postTo(project)
  }
}
