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
package com.zerocracy.stk.pm.in.orders

import com.jcabi.github.Github
import com.jcabi.github.Issue
import com.jcabi.log.Logger
import com.jcabi.xml.XML
import com.zerocracy.entry.ExtGithub
import com.zerocracy.farm.Assume
import com.zerocracy.jstk.Farm
import com.zerocracy.jstk.Project
import com.zerocracy.pm.ClaimIn
import com.zerocracy.pm.ClaimOut
import com.zerocracy.radars.github.Job

def exec(Project project, XML xml) {
  new Assume(project, xml).type('Order was cancelled')
  ClaimIn claim = new ClaimIn(xml)
  String job = claim.param('job')
  if (!job.startsWith('gh:')) {
    return
  }
  Farm farm = binding.variables.farm
  Github github = new ExtGithub(farm).value()
  Issue.Smart issue = new Issue.Smart(new Job.Issue(github, job))
  String login = claim.param('login')
  if (issue.json().isNull('assignee')) {
    Logger.warn(
      this, 'Issue %s#%d doesn\'t have an assignee',
      issue.repo().coordinates(), issue.number()
    )
  } else if (login == issue.assignee().login().toLowerCase(Locale.ENGLISH)) {
    issue.assign('')
    new ClaimOut()
      .type('GitHub issue lost an assignee')
      .param('login', login)
      .param('repo', issue.repo().coordinates())
      .param('issue', issue.number())
      .postTo(project)
  } else {
    Logger.warn(
      this, 'Issue %s#%d has a different assignee already: @%s',
      issue.repo().coordinates(), issue.number(),
      issue.assignee().login()
    )
  }
}
