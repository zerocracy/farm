/*
 * Copyright (c) 2016-2019 Zerocracy
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
import com.jcabi.github.Pull
import com.jcabi.xml.XML
import com.zerocracy.*
import com.zerocracy.claims.ClaimIn
import com.zerocracy.entry.ClaimsOf
import com.zerocracy.entry.ExtGithub
import com.zerocracy.farm.Assume
import com.zerocracy.farm.props.Props
import com.zerocracy.pm.staff.Roles
import com.zerocracy.radars.github.Job

def exec(Project project, XML xml) {
  new Assume(project, xml).notPmo()
  new Assume(project, xml).type('Job removed from WBS')
  ClaimIn claim = new ClaimIn(xml)
  String job = claim.param('job')
  if (!job.startsWith('gh:')) {
    return
  }
  Farm farm = binding.variables.farm
  Github github = new ExtGithub(farm).value()
  Issue.Smart issue = new Issue.Smart(new Job.Issue(github, job))
  if (!issue.pull) {
    return
  }
  Pull pull = issue.pull()
  // @todo #1897:30min MkPull.merge is not implemented in jcabi github library
  //  let's submit a bug to jcabi, wait for fix and remove this check for testing mode.
  //  MkPull.merge should change 'merged' boolean flag in pull JSON, see github docs.
  //  Also jcabi can implement Pull.Smart.merged() method to check that PR was merged.
  boolean merged
  if (new Props(farm).has('//testing')) {
    merged = true
  } else {
    merged = pull.json().getBoolean('merged', false)
  }
  if (!merged) {
    throw new SoftException(
      new Par(
        farm,
        'Pull request %s was not merged, no payment for ARC, see §28'
      ).say(job)
    )
  }
  List<String> logins = new Roles(project).bootstrap().findByRole('ARC')
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
