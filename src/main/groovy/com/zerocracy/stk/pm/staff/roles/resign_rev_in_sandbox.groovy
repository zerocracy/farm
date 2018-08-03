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

def exec(Project project, XML xml) {
  new Assume(project, xml).notPmo()
  new Assume(project, xml).type('Ping hourly')
  Farm farm = binding.variables.farm
  Catalog catalog = new Catalog(farm).bootstrap()
  if (!catalog.sandbox().contains(project.pid())) {
    return
  }
  ClaimIn claim = new ClaimIn(xml)
  int threshold = new Policy().get('33.rev-min-rep', 128)
  Roles roles = new Roles(project).bootstrap()
  roles.findByRole('REV').each { uid ->
    int reputation = new Awards(farm, uid).bootstrap().total()
    if (reputation > threshold) {
      return
    }
    claim.copy()
      .type('Resign role')
      .param('login', uid)
      .param('role', 'REV')
      .param('reason', 'Reputation is too low')
      .postTo(new ClaimsOf(farm, project))
    claim.copy().type('Notify project').param(
      'message', new Par(
        'I decided to resign REV role from @%s,',
        'because the reputation of the user is %+d, which is below %+d;',
        'according to §33 he/she can\'t be a reviewer in a sandbox project'
      ).say(uid, reputation, threshold)
    ).postTo(new ClaimsOf(farm, project))
    claim.copy().type('Notify user').token("user;${uid}").param(
      'message', new Par(
        farm,
        'Your reputation is %+d (below %+d);',
        'according to §33 you can\'t be a reviewer anymore in %s'
      ).say(reputation, threshold, project.pid())
    ).postTo(new ClaimsOf(farm, project))
    claim.copy().type('Notify PMO').param(
      'message', new Par(
        farm,
        'The user @%s was resigned from REV in %s',
        'because of too low reputation %+d (below %+d)'
      ).say(uid, project.pid(), reputation, threshold)
    ).postTo(new ClaimsOf(farm, project))
  }
}
