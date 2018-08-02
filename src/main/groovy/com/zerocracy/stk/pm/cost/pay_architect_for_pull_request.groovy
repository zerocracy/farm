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
package com.zerocracy.stk.pm.cost

import com.jcabi.github.Github
import com.jcabi.github.Issue
import com.jcabi.xml.XML
import com.zerocracy.Farm
import com.zerocracy.Par
import com.zerocracy.Policy
import com.zerocracy.Project
import com.zerocracy.entry.ClaimsOf
import com.zerocracy.entry.ExtGithub
import com.zerocracy.farm.Assume
import com.zerocracy.claims.ClaimIn
import com.zerocracy.pm.staff.Roles
import com.zerocracy.radars.github.Job

def exec(Project project, XML xml) {
  new Assume(project, xml).notPmo()
  new Assume(project, xml).type('Order was finished')
  ClaimIn claim = new ClaimIn(xml)
  String job = claim.param('job')
  String performer = claim.param('login')
  if (!job.startsWith('gh:')) {
    return
  }
  List<String> logins = new Roles(project).bootstrap().findByRole('ARC')
  Farm farm = binding.variables.farm
  if (logins.empty) {
    claim.copy()
      .type('Notify project')
      .param(
        'message',
        new Par(
          'There are no ARC roles in the project,',
          'I can\'t pay for the pull request review by §28: %s',
        ).say(job)
      )
      .postTo(new ClaimsOf(farm, project))
    return
  }
  if (logins.size() > 1) {
    claim.copy()
      .type('Notify project')
      .param(
        'message',
        new Par(
          'There are too many ARC roles in the project (more than one),',
          'I can\'t pay for the pull request review by §28: %s'
        ).say(job)
      )
      .postTo(new ClaimsOf(farm, project))
    return
  }
  String arc = logins[0]
  if (arc == performer) {
    return
  }
  Github github = new ExtGithub(farm).value()
  Issue.Smart issue = new Issue.Smart(new Job.Issue(github, job))
  if (issue.pull) {
    claim.copy()
      .type('Make payment')
      .param('job', job)
      .param('login', arc)
      .param(
        'reason',
        new Par(
          'Payment to ARC for a closed pull request, as in §28'
        ).say()
      )
      .param('minutes', new Policy().get('28.price', 10))
      .postTo(new ClaimsOf(farm, project))
  }
}
