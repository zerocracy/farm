package com.zerocracy.stk.pm.time.reminders

import com.jcabi.xml.XML
import com.zerocracy.farm.Assume
import com.zerocracy.jstk.Project
import com.zerocracy.pm.ClaimIn
import com.zerocracy.pm.ClaimOut
import com.zerocracy.pm.in.Orders
import java.time.ZoneOffset
import java.time.ZonedDateTime

def exec(Project project, XML xml) {
  new Assume(project, xml).type('New reminder posted')
  new Assume(project, xml).notPmo()
  def claim = new ClaimIn(xml)

  def claimTime = ZonedDateTime.ofInstant(
    claim.created().toInstant(), ZoneOffset.UTC
  )
  def orders = new Orders(project).bootstrap()
  orders.olderThan(claimTime.minusDays(10)).forEach {
    def login = orders.performer(it)
    orders.resign(it)
    new ClaimOut()
      .type('Notify job')
      .token("job;$it")
      .param(
      'message',
      "@$login this job was resigned from you after 10 days (this is our policy)."
    ).postTo(project)
  }
}
