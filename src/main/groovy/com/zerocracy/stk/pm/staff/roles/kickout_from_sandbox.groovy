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
package com.zerocracy.stk.pm.staff.roles

import com.jcabi.xml.XML
import com.zerocracy.Farm
import com.zerocracy.Par
import com.zerocracy.Policy
import com.zerocracy.Project
import com.zerocracy.entry.ClaimsOf
import com.zerocracy.farm.Assume
import com.zerocracy.claims.ClaimIn
import com.zerocracy.pm.staff.Roles
import com.zerocracy.pmo.Awards
import com.zerocracy.pmo.Catalog
import com.zerocracy.pmo.People

def exec(Project project, XML xml) {
  new Assume(project, xml).notPmo()
  new Assume(project, xml).type('Ping hourly')
  Farm farm = binding.variables.farm
  Catalog catalog = new Catalog(farm).bootstrap()
  if (!catalog.sandbox().contains(project.pid())) {
    return
  }
  ClaimIn claim = new ClaimIn(xml)
  People people = new People(farm).bootstrap()
  Roles roles = new Roles(project).bootstrap()
  roles.everybody().each { uid ->
    if (!people.hasMentor(uid)) {
      return
    }
    if (roles.hasRole(uid, 'PO', 'ARC')) {
      return
    }
    if (roles.hasRole(uid, 'QA')) {
      return
    }
    int reputation = new Awards(farm, uid).bootstrap().total()
    int threshold = new Policy().get('33.sandbox-rep-threshold', 1024)
    if (reputation < threshold) {
      return
    }
    claim.copy()
      .type('Resign all roles')
      .param('login', uid)
      .param(
        'reason',
        new Par('Reputation %s of @%s is over %s').say(
          reputation, uid, threshold
        )
      )
      .postTo(new ClaimsOf(farm, project))
    claim.copy().type('Notify user').token("user;${uid}").param(
      'message',
      new Par(
        'Your reputation is %d (over %d);',
        'according to §33 you may not work in sandbox projects anymore;',
        'we resigned you from project %s;',
        'the jobs, which you may still have there, are still yours,',
        'feel free to complete them;',
        'you are welcome to join [other](/board) non-sandbox projects!'
      ).say(reputation, threshold, project.pid())
    ).postTo(new ClaimsOf(farm, project))
    claim.copy().type('Notify PMO').param(
      'message', new Par(
        'The user @%s was kicked out of sandbox project %s',
        'because of too high reputation %d (over %d)'
      ).say(uid, project.pid(), reputation, threshold)
    ).postTo(new ClaimsOf(farm, project))
  }
}
