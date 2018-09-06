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
package com.zerocracy.stk.pm.scope.wbs

import com.jcabi.github.Github
import com.jcabi.github.Issue
import com.jcabi.xml.XML
import com.zerocracy.Farm
import com.zerocracy.Par
import com.zerocracy.Project
import com.zerocracy.SoftException
import com.zerocracy.entry.ClaimsOf
import com.zerocracy.entry.ExtGithub
import com.zerocracy.farm.Assume
import com.zerocracy.claims.ClaimIn
import com.zerocracy.pm.scope.Wbs
import com.zerocracy.pm.staff.Roles
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
  if (job.startsWith('gh:')) {
    Issue issue = new Issue.Smart(new Job.Issue(github, job))
    if (issue.pull) {
      role = 'REV'
    }
    if (role == 'REV' && issue.pull) {
      JsonObject pull = issue.pull().json()
      int lines = pull.getInt('additions', 0) + pull.getInt('deletions', 0)
      int min = 10
      if (lines < min) {
        throw new SoftException(
          new Par(
            '@%s this pull request is too small,',
            'just %d lines changed (less than %d),',
            'there will be no formal code review, see §53 and §28;',
            'in the future, try to make sure your pull requests are not too small;',
            '@%s please review this and merge or reject'
          ).say(issue.author().login(), lines, min, new Roles(project).bootstrap().findByRole('ARC')[0])
        )
      }
    }
  }
  wbs.add(job)
  wbs.role(job, role)
  claim.reply(
    new Par('Job %s is now in scope, role is %s').say(job, role)
  ).postTo(new ClaimsOf(farm, project))
  claim.copy()
    .type('Job was added to WBS')
    .param('role', role)
    .postTo(new ClaimsOf(farm, project))
}
