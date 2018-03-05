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
import com.zerocracy.Project
import com.zerocracy.entry.ExtGithub
import com.zerocracy.farm.Assume
import com.zerocracy.pm.ClaimIn
import com.zerocracy.pm.in.Orders
import com.zerocracy.pm.staff.Roles
import com.zerocracy.radars.github.Job

import java.security.SecureRandom

def exec(Project project, XML xml) {
  new Assume(project, xml).notPmo()
  new Assume(project, xml).type('Close job')
  ClaimIn claim = new ClaimIn(xml)
  String job = claim.param('job')
  Farm farm = binding.variables.farm
  Github github = new ExtGithub(farm).value()
  if (job.startsWith('gh:') && new Issue.Smart(new Job.Issue(github, job)).open) {
    return
  }
  Orders orders = new Orders(project).bootstrap()
  if (orders.assigned(job)) {
    List<String> qa = new Roles(project).bootstrap().findByRole('QA')
    if (qa.empty) {
      claim.copy()
        .type('Finish order')
        .param('reason', 'GitHub issue was closed, order is finished')
        .postTo(project)
    } else {
      String inspector
      if (qa.size() > 1) {
        inspector = qa[new SecureRandom().nextInt(qa.size() - 1)]
      } else {
        inspector = qa.first()
      }
      claim.copy()
        .type('Assign QA inspector')
        .param('assignee', inspector)
        .postTo(project)
    }
  } else {
    claim.copy()
      .type('Remove job from WBS')
      .postTo(project)
  }
}
