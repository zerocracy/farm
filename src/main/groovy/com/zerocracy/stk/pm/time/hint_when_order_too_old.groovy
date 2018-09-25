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
package com.zerocracy.stk.pm.time

import com.jcabi.xml.XML
import com.zerocracy.Farm
import com.zerocracy.Par
import com.zerocracy.Project
import com.zerocracy.farm.Assume
import com.zerocracy.claims.ClaimIn
import com.zerocracy.pm.cost.Ledger
import com.zerocracy.pm.in.Impediments
import com.zerocracy.pm.in.Orders
import com.zerocracy.pm.staff.Roles
import com.zerocracy.pmo.Hint
import com.zerocracy.pmo.Pmo
import java.time.ZoneOffset
import java.time.ZonedDateTime

def exec(Project project, XML xml) {
  new Assume(project, xml).notPmo()
  new Assume(project, xml).type('Ping hourly')
  if (new Ledger(farm, project).bootstrap().deficit()) {
    // We must not remind anyone if the project is not funded now. Simply
    // because we can't force any actions at the moment. We will remind,
    // but developers will be stuck anyway. They may lose their jobs
    // because of that.
    return
  }
  ClaimIn claim = new ClaimIn(xml)
  ZonedDateTime now = ZonedDateTime.ofInstant(
    claim.created().toInstant(), ZoneOffset.UTC
  )
  Orders orders = new Orders(farm, project).bootstrap()
  Impediments impediments = new Impediments(farm, project).bootstrap()
  Farm farm = binding.variables.farm
  Roles pmos = new Roles(new Pmo(farm)).bootstrap()
  orders.olderThan(now.minusDays(5)).each { job ->
    if (impediments.exists(job)) {
      return
    }
    String login = orders.performer(job)
    if (pmos.hasAnyRole(login)) {
      // Members of PMO have special status, we should not bother them
      // with any reminders, ever.
      return
    }
    new Hint(
      farm,
      Integer.MAX_VALUE,
      claim.copy()
        .type('Notify job')
        .token("job;${job}")
        .param('mnemo', "Order of @${orders.performer(job)} is too old")
        .param(
          'message',
          new Par(
            '@%s this job was assigned to you %[ms]s ago.',
            'It will be taken away from you soon, unless you close it, see §8.',
            'Read [this](/2014/04/13/no-obligations-principle.html)',
            'and [this](/2014/11/24/principles-of-bug-tracking.html), please.'
          ).say(orders.performer(job), System.currentTimeMillis() - orders.startTime(job).time)
        )
    ).postTo(project)
  }
}
