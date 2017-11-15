package com.zerocracy.stk.pm.in.orders

import com.jcabi.xml.XML
import com.zerocracy.farm.Assume
import com.zerocracy.jstk.Project
import com.zerocracy.pm.ClaimIn
import com.zerocracy.pm.ClaimOut
import com.zerocracy.pm.in.Orders
import java.time.ZoneOffset
import java.time.ZonedDateTime

def exec(Project project, XML xml) {
  new Assume(project, xml).type('Ping')
  new Assume(project, xml).notPmo()
  ClaimIn claim = new ClaimIn(xml)
  ZonedDateTime time = ZonedDateTime.ofInstant(
    claim.created().toInstant(), ZoneOffset.UTC
  )
  Orders orders = new Orders(project).bootstrap()
  int days = 10
  orders.olderThan(time.minusDays(days)).forEach {
    new ClaimOut()
      .type('Cancel order')
      .token("job;$it")
      .param('job', it)
      .param('reason', "It is older than ${days} days")
      .postTo(project)
  }
}
