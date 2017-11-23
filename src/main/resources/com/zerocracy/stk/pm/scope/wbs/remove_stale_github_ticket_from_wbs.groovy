/**
 * Copyright (c) 2016-2017 Zerocracy
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
package com.zerocracy.stk.pm.scope.wbs

import com.jcabi.github.Event
import com.jcabi.github.Github
import com.jcabi.github.Issue
import com.jcabi.xml.XML
import com.zerocracy.entry.ExtGithub
import com.zerocracy.farm.Assume
import com.zerocracy.jstk.Farm
import com.zerocracy.jstk.Project
import com.zerocracy.pm.ClaimOut
import com.zerocracy.pm.in.Orders
import com.zerocracy.pm.scope.Wbs
import com.zerocracy.radars.github.Job
import java.time.LocalDate

def exec(Project project, XML xml) {
  new Assume(project, xml).type('Ping')
  Wbs wbs = new Wbs(project).bootstrap()
  Orders orders = new Orders(project).bootstrap()
  Farm farm = binding.variables.farm
  Github github = new ExtGithub(farm).value()
  Date threshold = java.sql.Date.valueOf(LocalDate.now().minusDays(5))
  for (String job : wbs.iterate()) {
    if (orders.assigned(job)) {
      continue
    }
    Issue.Smart issue = new Issue.Smart(new Job.Issue(github, job))
    if (issue.open) {
      continue
    }
    Date closed = new Event.Smart(issue.latestEvent(Event.CLOSED)).createdAt()
    if (closed > threshold) {
      continue
    }
    new ClaimOut()
      .type('Remove job from WBS')
      .param('job', job)
      .param('reason', 'GitHub issue is already closed')
      .postTo(project)
  }
}
