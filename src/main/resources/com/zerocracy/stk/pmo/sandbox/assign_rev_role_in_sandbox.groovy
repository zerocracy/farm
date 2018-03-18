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
package com.zerocracy.stk.pmo.sandbox

import com.jcabi.xml.XML
import com.zerocracy.Farm
import com.zerocracy.Par
import com.zerocracy.Policy
import com.zerocracy.Project
import com.zerocracy.farm.Assume
import com.zerocracy.pm.ClaimIn
import com.zerocracy.pm.staff.Roles
import com.zerocracy.pmo.Awards
import com.zerocracy.pmo.Catalog

def exec(Project pmo, XML xml) {
  new Assume(pmo, xml).isPmo()
  new Assume(pmo, xml).type('Ping hourly')
  Catalog catalog = new Catalog(pmo).bootstrap()
  Farm farm = binding.variables.farm
  ClaimIn claim = new ClaimIn(xml)
  int threshold = new Policy().get('33.rev-rep', 256)
  catalog.sandbox().each { pid ->
    Project project = farm.find("@id='${pid}'")[0]
    Roles roles = new Roles(project).bootstrap()
    roles.findByRole('DEV').each { uid ->
      if (roles.hasRole(uid, 'REV')) {
        return
      }
      int reputation = new Awards(pmo, uid).bootstrap().total()
      if (reputation < threshold) {
        return
      }
      claim.copy()
        .type('Assign role')
        .param('login', uid)
        .param('role', 'REV')
        .postTo(project)
      claim.copy().type('Notify user').token("user;${uid}").param(
        'message',
        new Par(
          farm,
          'Your reputation is %+d (over %+d);',
          'according to §33 you are now a code reviewer in %s'
        ).say(reputation, threshold, pid)
      ).postTo(pmo)
      claim.copy().type('Notify PMO').param(
        'message', new Par(
          farm,
          'The user @%s was promoted to REV in %s',
          'because of high enough reputation %+d (over %+d)'
        ).say(uid, pid, reputation, threshold)
      ).postTo(pmo)
    }
  }
}
