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
package com.zerocracy.stk.pm.staff.elections

import com.jcabi.log.Logger
import com.jcabi.xml.XML
import com.zerocracy.Farm
import com.zerocracy.Project
import com.zerocracy.farm.Assume
import com.zerocracy.pm.ClaimIn
import com.zerocracy.pm.ClaimOut
import com.zerocracy.pm.Claims
import com.zerocracy.pm.cost.Boosts
import com.zerocracy.pm.cost.Ledger
import com.zerocracy.pm.scope.Wbs
import com.zerocracy.pm.staff.Elections
import com.zerocracy.pm.staff.Roles
import com.zerocracy.pm.staff.ranks.RnkBoost
import com.zerocracy.pm.staff.ranks.RnkRev
import com.zerocracy.pm.staff.votes.VsBanned
import com.zerocracy.pm.staff.votes.VsNoRoom
import com.zerocracy.pm.staff.votes.VsRate
import com.zerocracy.pm.staff.votes.VsSpeed
import com.zerocracy.pm.staff.votes.VsVacation
import com.zerocracy.pm.staff.votes.VsWorkload
import com.zerocracy.pmo.Pmo

def exec(Project project, XML xml) {
  new Assume(project, xml).notPmo()
  new Assume(project, xml).type('Ping')
  Claims claims = new Claims(project)
  if (!claims.iterate().empty && !new ClaimIn(xml).hasParam('force')) {
    Logger.info(this, 'Still %d claims, can\'t elect', claims.iterate().size())
    return
  }
  if (new Ledger(project).bootstrap().deficit()) {
    return
  }
  Wbs wbs = new Wbs(project).bootstrap()
  Roles roles = new Roles(project).bootstrap()
  Elections elections = new Elections(project).bootstrap()
  Farm farm = binding.variables.farm
  Project pmo = new Pmo(farm)
  def jobs = wbs.iterate().toList().with {
    lst ->
      [
        new RnkBoost(new Boosts(project).bootstrap()),
        new RnkRev(new Wbs(project).bootstrap())
      ].each { lst.sort(it) }
      lst
  }

  for (String job : jobs) {
    String role = wbs.role(job)
    List<String> logins = roles.findByRole(role)
    if (logins.empty) {
      Logger.info(this, 'No %ss in %s, cannot elect', role, project.pid())
      return
    }
    boolean done = elections.elect(
      job, logins,
      [
        (new VsRate(project, logins)): 2,
        (new VsNoRoom(pmo))          : role == 'REV' ? 0 : -100,
        (new VsBanned(project, job)) : -100,
        (new VsVacation(pmo))        : -100,
        (new VsWorkload(pmo, logins)): 1,
        (new VsSpeed(pmo, logins))   : 3
      ]
    )
    if (done && elections.elected(job)) {
      new ClaimOut()
        .type('Performer was elected')
        .param('login', elections.winner(job))
        .param('job', job)
        .param('role', role)
        .param('reason', elections.reason(job))
        .postTo(project)
      break
    }
  }
}
