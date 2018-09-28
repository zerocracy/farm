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
package com.zerocracy.stk.pm.staff

import com.jcabi.xml.XML
import com.zerocracy.Farm
import com.zerocracy.Par
import com.zerocracy.Project
import com.zerocracy.farm.Assume
import com.zerocracy.claims.ClaimIn
import com.zerocracy.pm.in.Orders
import com.zerocracy.pm.scope.Wbs
import com.zerocracy.pmo.Catalog
import com.zerocracy.pmo.Hint
import java.util.concurrent.TimeUnit

def exec(Project project, XML xml) {
  new Assume(project, xml).notPmo()
  new Assume(project, xml).type('Ping hourly')
  Farm farm = binding.variables.farm
  if (!new Catalog(farm).published(project.pid())) {
    return
  }
  ClaimIn claim = new ClaimIn(xml)
  Wbs wbs = new Wbs(project).bootstrap()
  Orders orders = new Orders(farm, project).bootstrap()
  int pending = 0
  wbs.iterate().each { job ->
    Date created = wbs.created(job)
    if (created.time > System.currentTimeMillis() - TimeUnit.DAYS.toMillis(2L)) {
      return
    }
    if (orders.assigned(job)) {
      return
    }
    pending++
  }
  if (pending < 5) {
    return
  }
  new Hint(
    farm,
    (int) TimeUnit.DAYS.toSeconds(5L),
    claim.copy()
      .type('Tweet')
      .token("project;${project.pid()}")
      .param('mnemo', 'Looking for developers on Twitter')
      .param(
        'par', new Par(
          farm,
          'The project %s is looking for developers,',
          'there are %d+ tasks waiting for your contribution:',
          'https://www.0crat.com/p/%1$s #remotework #job #freelance'
        ).say(project.pid(), pending)
      )
  ).postTo(project)
}
