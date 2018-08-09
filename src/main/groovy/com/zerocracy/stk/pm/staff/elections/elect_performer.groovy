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
package com.zerocracy.stk.pm.staff.elections

import com.jcabi.github.Github
import com.jcabi.xml.XML
import com.zerocracy.Farm
import com.zerocracy.Policy
import com.zerocracy.Project
import com.zerocracy.claims.ClaimIn
import com.zerocracy.entry.ClaimsOf
import com.zerocracy.entry.ExtGithub
import com.zerocracy.farm.Assume
import com.zerocracy.pm.cost.Boosts
import com.zerocracy.pm.cost.Ledger
import com.zerocracy.pm.in.Orders
import com.zerocracy.pm.qa.Reviews
import com.zerocracy.pm.scope.Wbs
import com.zerocracy.pm.staff.ElectionResult
import com.zerocracy.pm.staff.Elections
import com.zerocracy.pm.staff.Roles
import com.zerocracy.pm.staff.ranks.RnkBoost
import com.zerocracy.pm.staff.ranks.RnkGithubBug
import com.zerocracy.pm.staff.ranks.RnkGithubMilestone
import com.zerocracy.pm.staff.ranks.RnkRev
import com.zerocracy.pm.staff.votes.*
import com.zerocracy.pmo.Pmo

def exec(Project project, XML xml) {
  new Assume(project, xml).notPmo()
  new Assume(project, xml).type('Ping')
  ClaimIn claim = new ClaimIn(xml)
  if (new Ledger(project).bootstrap().deficit()) {
    return
  }
  // @todo #926:30min we should synchronize elected, but not assigned jobs
  //  between different projects, because one project may elect a user
  //  as a performer for few jobs and another project may elect same user
  //  before jobs from first project will be assigned to the performer.
  Wbs wbs = new Wbs(project).bootstrap()
  Roles roles = new Roles(project).bootstrap()
  Collection<String> orders = new Orders(project).bootstrap().iterate()
  Elections elections = new Elections(project).bootstrap()
  Collection<String> reviews = new Reviews(project).bootstrap().iterate()
  Farm farm = binding.variables.farm
  Pmo pmo = new Pmo(farm)
  Github github = new ExtGithub(farm).value()
  // @todo #1214:30min 0crat is assigning closed jobs. It happens when the
  //  issue was closed in github but the Close Job flow fails
  //  for some reason and the job does not leave WBS. Assure that we are
  //  iterating only in open issues when electing performer and uncomment
  //  _after.groovy tests in dont_assign_job_closed bundle.
  List<String> jobs = wbs.iterate().toList()
  [
      new RnkGithubBug(github),
      new RnkBoost(new Boosts(project).bootstrap()),
      new RnkGithubMilestone(github),
      new RnkRev(new Wbs(project).bootstrap())
  ].each { jobs.sort(it) }
  for (String job : jobs) {
    if (orders.contains(job) || reviews.contains(job)) {
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
    int max = new Policy().get('3.absolute-max', 32)
    boolean done = elections.elect(
      job, logins,
      [
        (new VsSafe(new VsHardCap(pmo, max)))                                     : -100,
//        (new VsSafe(new VsOverElected(project, farm)))                            : role == 'REV' ? 0 : -100,
        (new VsSafe(new VsReputation(pmo, logins)))                               : 4,
        (new VsSafe(new VsLosers(pmo, new Policy().get('3.low-threshold', -128)))): -100,
        (new VsSafe(new VsRate(project, logins)))                                 : 2,
        (new VsSafe(new VsBigDebt(pmo)))                                          : -100,
        (new VsSafe(new VsNoRoom(pmo)))                                           : role == 'REV' ? 0 : -100,
        (new VsSafe(new VsOptionsMaxJobs(pmo)))                                   : role == 'REV' ? 0 : -100,
        (new VsSafe(new VsBanned(project, job)))                                  : -100,
        (new VsSafe(new VsVacation(pmo)))                                         : -100,
        (new VsSafe(new VsWorkload(farm, logins)))                                : 1,
        (new VsSafe(new VsWorkload(farm, project, logins)))                       : 1,
        (new VsSafe(new VsSpeed(pmo, logins)))                                    : 3,
        (new VsSafe(new VsBalance(project, farm, logins)))                        : 3,
        (new VsSafe(new VsRandom()))                                              : 1
      ]
    )
    ElectionResult result = elections.result(job)
    if (done && result.elected()) {
      claim.copy()
        .type('Performer was elected')
        .param('login', result.winner())
        .param('job', job)
        .param('role', role)
        .param('reason', result.reason())
        .postTo(new ClaimsOf(farm, project))
      break
    }
  }
}
