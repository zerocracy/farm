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
import com.zerocracy.pmo.People
import com.zerocracy.pmo.Projects

def exec(Project pmo, XML xml) {
  new Assume(pmo, xml).isPmo()
  new Assume(pmo, xml).type('Ping hourly')
  People people = new People(pmo).bootstrap()
  Catalog catalog = new Catalog(pmo).bootstrap()
  Farm farm = binding.variables.farm
  ClaimIn claim = new ClaimIn(xml)
  people.iterate().each { uid ->
    if (!people.hasMentor(uid)) {
      return
    }
    int reputation = new Awards(pmo, uid).bootstrap().total()
    int threshold = new Policy().get('33.sandbox-rep-threshold', 1024)
    if (reputation < threshold) {
      return
    }
    Projects projects = new Projects(pmo, uid).bootstrap()
    projects.iterate().each { pid ->
      if (!catalog.sandbox().contains(pid)) {
        return
      }
      Project pkt = farm.find("@id='${pid}'")[0]
      Roles roles = new Roles(pkt).bootstrap()
      if (roles.hasRole(uid, 'PO', 'ARC')) {
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
        .postTo(pkt)
      claim.copy().type('Notify user').token("user;${uid}").param(
        'message',
        new Par(
          'Your reputation is %d (over %d);',
          'according to §33 you may not work in sandbox projects anymore;',
          'we resigned you from project %s;',
          'the jobs, which you may still have there, are still yours,',
          'feel free to complete them;',
          'you are welcome to join [other](/board) non-sandbox projects!'
        ).say(reputation, threshold, pid)
      ).postTo(pmo)
      claim.copy().type('Notify PMO').param(
        'message', new Par(
          'The user @%s was kicked out of sandbox project %s',
          'because of too high reputation %d (over %d)'
        ).say(uid, pid, reputation, threshold)
      ).postTo(pmo)
    }
  }
}
