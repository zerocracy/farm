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
import com.zerocracy.Policy
import com.zerocracy.Project
import com.zerocracy.farm.Assume
import com.zerocracy.pm.ClaimIn
import com.zerocracy.pm.Claims
import com.zerocracy.pm.cost.Boosts
import com.zerocracy.pm.cost.Ledger
import com.zerocracy.pm.in.Orders
import com.zerocracy.pm.qa.Reviews
import com.zerocracy.pm.scope.Wbs
import com.zerocracy.pm.staff.Elections
import com.zerocracy.pm.staff.Roles
import com.zerocracy.pm.staff.ranks.RnkBoost
import com.zerocracy.pm.staff.ranks.RnkRev
import com.zerocracy.pm.staff.votes.VsBanned
import com.zerocracy.pm.staff.votes.VsHardCap
import com.zerocracy.pm.staff.votes.VsLosers
import com.zerocracy.pm.staff.votes.VsNoRoom
import com.zerocracy.pm.staff.votes.VsRandom
import com.zerocracy.pm.staff.votes.VsRate
import com.zerocracy.pm.staff.votes.VsReputation
import com.zerocracy.pm.staff.votes.VsSafe
import com.zerocracy.pm.staff.votes.VsSpeed
import com.zerocracy.pm.staff.votes.VsVacation
import com.zerocracy.pm.staff.votes.VsWorkload
import com.zerocracy.pmo.Pmo

def exec(Project project, XML xml) {
  new Assume(project, xml).notPmo()
  new Assume(project, xml).type('Ping')
  ClaimIn claim = new ClaimIn(xml)
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
  Orders orders = new Orders(project).bootstrap()
  Elections elections = new Elections(project).bootstrap()
  Reviews reviews = new Reviews(project).bootstrap()
  Farm farm = binding.variables.farm
  Project pmo = new Pmo(farm)
  List<String> jobs = wbs.iterate().toList()
  [
    // new RnkGithubBug(github),
    new RnkBoost(new Boosts(project).bootstrap()),
    new RnkRev(new Wbs(project).bootstrap())
  ].each { jobs.sort(it) }
  for (String job : jobs) {
    if (orders.assigned(job)) {
      continue
    }
    if (reviews.exists(job)) {
      continue
    }
    if (elections.exists(job)) {
      continue
    }
    String role = wbs.role(job)
    List<String> logins = roles.findByRole(role)
    if (logins.empty) {
      return
    }
    boolean done = elections.elect(
      job, logins,
      [
        (new VsSafe(new VsHardCap(pmo, new Policy().get('3.absolute-max', 32)))): -100,
        (new VsSafe(new VsReputation(pmo, logins)))                             : 5,
        (new VsSafe(new VsLosers(pmo, new Policy().get('3.low-threshold', -128)))) : -100,
        (new VsSafe(new VsRate(project, logins)))                               : 2,
        (new VsSafe(new VsNoRoom(pmo)))                                         : role == 'REV' ? 0 : -100,
        (new VsSafe(new VsBanned(project, job)))                                : -100,
        (new VsSafe(new VsVacation(pmo)))                                       : -100,
        (new VsSafe(new VsWorkload(pmo, logins)))                               : 1,
        (new VsSafe(new VsSpeed(pmo, logins)))                                  : 3,
        (new VsSafe(new VsRandom()))                                          : 1
      ]
    )
    if (done && elections.elected(job)) {
      claim.copy()
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
