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
import com.zerocracy.claims.ClaimIn
import com.zerocracy.entry.ClaimsOf
import com.zerocracy.entry.ExtGithub
import com.zerocracy.farm.Assume
import com.zerocracy.pm.staff.Roles
import com.zerocracy.radars.github.Job
import org.cactoos.list.Mapped

import java.time.Duration

/**
 * Penalize architect for long pull request.
 *
 * @param project Current project
 * @param xml Claim
 */
def exec(Project project, XML xml) {
  new Assume(project, xml).notPmo()
  new Assume(project, xml).type('Order was finished')
  ClaimIn claim = new ClaimIn(xml)
  String job = claim.param('job')
  Duration age = Duration.ofMinutes(Long.parseLong(claim.param('age')))
  if (!job.startsWith('gh:')) {
    return
  }
  List<String> arcs = new Roles(project).bootstrap().findByRole('ARC')
  Farm farm = binding.variables.farm
  if (arcs.empty) {
    return
  }
  Github github = new ExtGithub(farm).value()
  Issue.Smart issue = new Issue.Smart(new Job.Issue(github, job))
  Policy policy = new Policy(farm)
  Duration threshold = Duration.ofDays(policy.get('55.duration1', 8))
  Duration max = Duration.ofDays(policy.get('55.duration2', 16))
  long penalty = 0
  if (issue.pull && age > threshold) {
    if (age > max) {
      penalty = policy.get('55.penalty2', -60)
    } else {
      penalty = policy.get('55.penalty2', -15)
    }
  }
  if (penalty < 0) {
    arcs.each { arc ->
      claim.copy()
        .type('Add award points')
        .param('login', arc)
        .param('job', job)
        .param('minutes', penalty)
        .param('reason', new Par('Code review was too long, see §55').say())
        .postTo(new ClaimsOf(farm))
    }
    String logins = String.join(', ', new Mapped({ "@${it}" }, arcs))
    claim.copy()
      .type('Notify job')
      .token("job;${job}")
      .param(
      'message',
      new Par("Code review was too long (%d days), architects (${logins}) were penalized, see §55")
        .say(age.toDays())
    ).postTo(new ClaimsOf(farm, project))
  }
}
