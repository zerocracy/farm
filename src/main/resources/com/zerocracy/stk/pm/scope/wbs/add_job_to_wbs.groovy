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
package com.zerocracy.stk.pm.scope.wbs

import com.jcabi.github.Github
import com.jcabi.github.Issue
import com.jcabi.xml.XML
import com.zerocracy.Farm
import com.zerocracy.Par
import com.zerocracy.Project
import com.zerocracy.entry.ExtGithub
import com.zerocracy.farm.Assume
import com.zerocracy.pm.ClaimIn
import com.zerocracy.pm.scope.Wbs
import com.zerocracy.pmo.People
import com.zerocracy.radars.github.Job

import javax.json.JsonObject

def exec(Project project, XML xml) {
  new Assume(project, xml).notPmo()
  new Assume(project, xml).type('Add job to WBS')
  new Assume(project, xml).roles('ARC', 'PO')
  Farm farm = binding.variables.farm
  Github github = new ExtGithub(farm).value()
  ClaimIn claim = new ClaimIn(xml)
  Wbs wbs = new Wbs(project).bootstrap()
  String job = claim.param('job')
  if (claim.hasParam('quiet') && wbs.exists(job)) {
    return
  }
  String role = 'DEV'
  if (claim.hasParam('role')) {
    role = claim.param('role')
  }
  Issue issue = new Issue.Smart(new Job.Issue(github, job))
  if (role == 'REV' && issue.pull) {
    JsonObject pull = issue.pull().json()
    int lines = pull.getInt('additions') + pull.getInt('deletions')
    if (lines < 10) {
      claim.reply(
        new Par('This pull request is too small: skipping review')
          .say()
      )
      return
    }
  }
  wbs.add(job)
  wbs.role(job, role)
  claim.reply(
    new Par('Job %s is now in scope, role is %s').say(job, role)
  ).postTo(project)
  if (issue.hasAssignee()) {
    People people = new People(farm).bootstrap()
    Iterator<String> find = people.find('github', issue.assignee().login()).iterator()
    if (find.hasNext()) {
      String login = find.next()
      if (people.hasMentor(login)) {
        claim.copy()
          .type('Start order')
          .param('login', login)
          .param('reason', claim.cid())
          .postTo(project)
      }
    }
  }
  claim.copy()
    .type('Job was added to WBS')
    .param('job', job)
    .param('role', role)
    .postTo(project)
}
