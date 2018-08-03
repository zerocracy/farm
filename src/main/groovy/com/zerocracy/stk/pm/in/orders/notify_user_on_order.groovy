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

import com.jcabi.xml.XML
import com.zerocracy.Farm
import com.zerocracy.Par
import com.zerocracy.Project
import com.zerocracy.entry.ClaimsOf
import com.zerocracy.farm.Assume
import com.zerocracy.claims.ClaimIn
import com.zerocracy.pm.cost.Estimates

def exec(Project project, XML xml) {
  new Assume(project, xml).notPmo()
  new Assume(project, xml).type('Order was given')
  ClaimIn claim = new ClaimIn(xml)
  String job = claim.param('job')
  String role = claim.param('role')
  Estimates estimates = new Estimates(project).bootstrap()
  String tail
  if (estimates.exists(job)) {
    tail = new Par(
      'you will earn %s on completion'
    ).say(estimates.get(job))
  } else {
    tail = new Par('you won\'t earn any cash on completion').say()
  }
  Farm farm = binding.variables.farm
  claim.copy()
    .type('Notify user')
    .token("user;${claim.param('login')}")
    .param(
      'message',
      new Par(
        farm,
        'The job %s was assigned to you in %s as %s a minute ago;',
        'here is [why](/footprint/%2$s/%s);'
      ).say(job, project.pid(), role, claim.param('reason')) + tail
    )
    .postTo(new ClaimsOf(farm, project))
}
