package com.zerocracy.stk.pm.in.orders

import com.jcabi.xml.XML
import com.zerocracy.Par
import com.zerocracy.Project
import com.zerocracy.farm.Assume
import com.zerocracy.pm.ClaimIn
import com.zerocracy.pm.cost.Boosts
import com.zerocracy.pm.in.Impediments
import com.zerocracy.pm.in.Orders
import java.time.ZoneOffset
import java.time.ZonedDateTime
import org.cactoos.iterable.Filtered
import org.cactoos.iterable.Limited

def exec(Project project, XML xml) {
  new Assume(project, xml).notPmo()
  new Assume(project, xml).type('Ping')
  ClaimIn claim = new ClaimIn(xml)
  ZonedDateTime time = ZonedDateTime.ofInstant(
    claim.created().toInstant(), ZoneOffset.UTC
  )
  Orders orders = new Orders(project).bootstrap()
  Boosts boosts = new Boosts(project).bootstrap()
  Impediments impediments = new Impediments(project).bootstrap()
  List<String> waiting = new Impediments(project).bootstrap().jobs().toList()
  int days = 10
  new Limited<>(
    5,
    new Filtered(
      { job -> !waiting.contains(job) },
      orders.olderThan(time.minusDays(days))
    )
  ).forEach { String job ->
    String worker = orders.performer(job)
    if (worker == 'yegor256') {
      return
    }
    if (impediments.exists(job)) {
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
