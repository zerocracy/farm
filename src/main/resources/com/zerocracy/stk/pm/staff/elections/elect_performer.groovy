/**
 * Copyright (c) 2016-2017 Zerocracy
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
package com.zerocracy.stk.pm.staff.elections

import com.jcabi.xml.XML
import com.zerocracy.farm.Assume
import com.zerocracy.jstk.Project
import com.zerocracy.pm.ClaimOut
import com.zerocracy.pm.hr.Roles
import com.zerocracy.pm.scope.Wbs
import com.zerocracy.pm.staff.Elections
import com.zerocracy.pm.staff.bans.FkBans
import com.zerocracy.pm.staff.voters.Banned
import com.zerocracy.pm.staff.voters.NoRoom

def exec(Project project, XML xml) {
  new Assume(project, xml).type('Ping')
  new Assume(project, xml).notPmo()
  Wbs wbs = new Wbs(project).bootstrap()
  Roles roles = new Roles(project).bootstrap()
  List<String> logins = roles.findByRole('DEV')
  if (!logins.empty) {
    Elections elections = new Elections(project).bootstrap()
    for (String job : wbs.iterate()) {
      def elected = elections.elect(
        job, logins,
        [
          (new NoRoom(project)): -100,
          (new Banned(job, new FkBans())): -1000
        ]
      )
      if (elected) {
        new ClaimOut()
          .type('Performer was elected')
          .param('login', elections.winner(job))
          .param('job', job)
          .param('reason', elections.reason(job))
          .postTo(project)
      }
    }
  }
}
