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
package com.zerocracy.stk.pm

import com.jcabi.log.Logger
import com.jcabi.xml.XML
import com.zerocracy.Par
import com.zerocracy.Project
import com.zerocracy.farm.Assume
import com.zerocracy.pm.ClaimIn
import com.zerocracy.pm.cost.Estimates
import com.zerocracy.pm.in.Impediments
import com.zerocracy.pm.in.Orders
import com.zerocracy.pm.scope.Wbs
import com.zerocracy.pm.staff.Bans
import org.cactoos.list.ListOf

def exec(Project project, XML xml) {
  new Assume(project, xml).notPmo()
  new Assume(project, xml).type('Check job status')
  ClaimIn claim = new ClaimIn(xml)
  String job = claim.param('job')
  Collection<String> items = []
  Wbs wbs = new Wbs(project).bootstrap()
  if (wbs.exists(job)) {
    items.add(
      new Par(
        'The job %s is in scope for ' +
        Logger.format(
          '%[ms]s',
          System.currentTimeMillis() - wbs.created(job).time
        )
      ).say(job)
    )
    items.add(new Par('The role is %s').say(wbs.role(job)))
    Orders orders = new Orders(project).bootstrap()
    if (orders.assigned(job)) {
      items.add(
        new Par(
          'The job is assigned to @%s for ' +
          Logger.format(
            '%[ms]s',
            System.currentTimeMillis() - orders.startTime(job).time
          )
      ).say(orders.performer(job)))
    } else {
      items.add(new Par('The job is not assigned to anyone').say())
    }
  } else {
    items.add(new Par('The job %s is not in scope').say(job))
  }
  Bans bans = new Bans(project).bootstrap()
  if (!bans.reasons(job).empty) {
    items.add(
      new Par(
        'These users are banned: ' +
        new Par.ToText(bans.reasons(job).join('; ')).toString()
      ).say()
    )
  }
  Estimates estimates = new Estimates(project).bootstrap()
  if (estimates.exists(job)) {
    items.add(
      new Par(
        'There is a monetary reward attached'
      ).say()
    )
  }
  Impediments impediments = new Impediments(project).bootstrap()
  if (new ListOf<>(impediments.jobs()).contains(job)) {
    items.add(new Par('The job has an impediment').say())
  }
  claim.reply(
    new Par(
      'This is what I know about this job, as in §32:'
    ).say() + '\n\n  * ' + items.join('\n  * ')
  ).postTo(project)
}
