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
package com.zerocracy.stk.pm.in.orders

import com.jcabi.github.Github
import com.jcabi.github.Issue
import com.jcabi.xml.XML
import com.zerocracy.Farm
import com.zerocracy.Par
import com.zerocracy.Project
import com.zerocracy.SoftException
import com.zerocracy.entry.ExtGithub
import com.zerocracy.farm.Assume
import com.zerocracy.pm.ClaimIn
import com.zerocracy.pm.in.Orders
import com.zerocracy.pm.scope.Wbs
import com.zerocracy.pm.staff.Roles
import com.zerocracy.radars.github.Job

def exec(Project project, XML xml) {
  new Assume(project, xml).notPmo()
  new Assume(project, xml).type('Close job')
  ClaimIn claim = new ClaimIn(xml)
  String job = claim.param('job')
  Wbs wbs = new Wbs(project).bootstrap()
  if (!wbs.exists(job)) {
    // @todo #536:30min Every time we close a ticket in GitHub, which was not a job,
    //  or when we close a pull request, which was not merged, we should
    //  add it to blanks.xml
    throw new SoftException(
      new Par('The job is not in WBS, won\'t close the order').say()
    )
  }
  if (job.startsWith('gh:')) {
    Farm farm = binding.variables.farm
    Github github = new ExtGithub(farm).value()
    Issue.Smart issue = new Issue.Smart(new Job.Issue(github, job))
    if (issue.open) {
      throw new SoftException(
        new Par('GitHub issue is still open, won\'t close').say()
      )
    }
    if (issue.author().login() != claim.author()
      && !new Roles(project).bootstrap().hasRole(claim.author(), 'PO', 'ARC')) {
      claim.copy()
        .type('Notify job')
        .token("job;${job}")
        .param(
          'message',
          new Par(
            'The issue is closed not by its creator,',
            'I won\'t close the order;',
            'please, re-open it and ask its creator to close it'
          ).say()
        )
        .postTo(project)
      return
    }
  }
  Orders orders = new Orders(project).bootstrap()
  if (orders.assigned(job)) {
    claim.copy()
      .type('Finish order')
      .param('reason', 'Job was closed, order is finished')
      .postTo(project)
  } else {
    claim.copy()
      .type('Remove job from WBS')
      .postTo(project)
  }
}
