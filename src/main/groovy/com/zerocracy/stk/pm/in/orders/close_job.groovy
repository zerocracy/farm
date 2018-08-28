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
package com.zerocracy.stk.pm.in.orders

import com.jcabi.github.Github
import com.jcabi.github.Issue
import com.jcabi.xml.XML
import com.zerocracy.Farm
import com.zerocracy.Par
import com.zerocracy.Project
import com.zerocracy.SoftException
import com.zerocracy.claims.ClaimIn
import com.zerocracy.entry.ClaimsOf
import com.zerocracy.entry.ExtGithub
import com.zerocracy.farm.Assume
import com.zerocracy.pm.in.Orders
import com.zerocracy.pm.scope.Wbs
import com.zerocracy.pm.staff.Roles
import com.zerocracy.pmo.Blanks
import com.zerocracy.radars.github.Job

def exec(Project project, XML xml) {
  new Assume(project, xml).notPmo()
  new Assume(project, xml).type('Close job')
  Farm farm = binding.variables.farm
  Github github = new ExtGithub(farm).value()
  ClaimIn claim = new ClaimIn(xml)
  String job = claim.param('job')
  Wbs wbs = new Wbs(project).bootstrap()
  Issue.Smart issue = new Issue.Smart(new Job.Issue(github, job))
  String author = issue.author().login().toLowerCase(Locale.ENGLISH)
  if (!wbs.exists(job)) {
    new Blanks(farm, author).bootstrap().add(project, job, issue.pull ? 'pull-request' : 'issue')
    claim.copy()
      .type('Job was declined')
      .token("job;${job}")
      .postTo(new ClaimsOf(farm, project))
  }
  Orders orders = new Orders(project).bootstrap()
  if (job.startsWith('gh:') && orders.assigned(job)) {
    if (issue.open) {
      throw new SoftException(
        new Par('GitHub issue is still open, won\'t close').say()
      )
    }
    if (author != claim.author()
      && !issue.pull
      && !new Roles(project).bootstrap().hasRole(claim.author(), 'PO', 'ARC')) {
      String message
      if (issue.open) {
        message = new Par(
          '@%s you are not allowed to close this job,',
          'I won\'t close the order;',
          'ask @%s (its author), ARC or PO do close it'
        ).say(claim.author(), issue.author().login())
      } else {
        message = new Par(
          '@%s the issue is closed not by @%s (its creator);',
          'I won\'t close the order;',
          'please, re-open it and ask @%2$s to close it'
        ).say(claim.author(), issue.author().login())
      }
      claim.copy()
        .type('Notify job')
        .token("job;${job}")
        .param('message', message)
        .postTo(new ClaimsOf(farm, project))
      return
    }
  }
  if (orders.assigned(job)) {
    claim.copy()
      .type('Finish order')
      .param('reason', 'Job was closed, order is finished')
      .param('closed', claim.created().toInstant().toString())
      .postTo(new ClaimsOf(farm, project))
  } else {
    claim.copy()
      .type('Remove job from WBS')
      .postTo(new ClaimsOf(farm, project))
  }
}
