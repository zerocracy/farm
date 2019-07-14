/*
 * Copyright (c) 2016-2019 Zerocracy
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
import com.jcabi.log.Logger
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
import com.zerocracy.pm.cost.Rates
import com.zerocracy.pm.in.Orders
import com.zerocracy.pm.qa.Reviews
import com.zerocracy.pm.scope.Wbs
import com.zerocracy.pm.staff.Bans
import com.zerocracy.pm.staff.Election
import com.zerocracy.pm.staff.ElectionResult
import com.zerocracy.pm.staff.Roles
import com.zerocracy.pm.staff.Votes
import com.zerocracy.pm.staff.ranks.*
import com.zerocracy.pm.staff.votes.*
import com.zerocracy.pmo.Pmo
import org.cactoos.iterable.Mapped

def exec(Project project, XML xml) {
  new Assume(project, xml).notPmo()
  new Assume(project, xml).type('Ping')
  ClaimIn claim = new ClaimIn(xml)
  boolean deficit = new Ledger(farm, project).bootstrap().deficit()
  Roles roles = new Roles(project).bootstrap()
  Rates rates = new Rates(project).bootstrap()
  int zeroRates = roles.everybody().count { uid -> !rates.exists(uid) }
  if (deficit && zeroRates == 0) {
    return
  }
  // @todo #926:30min we should synchronize elected, but not assigned jobs
  //  between different projects, because one project may elect a user
  //  as a performer for few jobs and another project may elect same user
  //  before jobs from first project will be assigned to the performer.
  Wbs wbs = new Wbs(project).bootstrap()
  Collection<String> orders = new Orders(farm, project).bootstrap().iterate()
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
  List<Comparator<String>> ranks = [
    new RnkMeasured(new RnkGithubLabel(github, 'pdd')),
    new RnkMeasured(new RnkGithubLabel(github, 'bug')),
    new RnkMeasured(new RnkBoost(new Boosts(farm, project).bootstrap())),
    new RnkMeasured(new RnkGithubMilestone(github)),
    new RnkMeasured(new RnkRev(new Wbs(project).bootstrap()))
  ]
  ranks.each { jobs.sort(it) }
  String ltag = 'com.zerocracy.election'
  if (Logger.isDebugEnabled(ltag)) {
    Logger.debug(
      ltag,
      'Election ranks metrics (project=%s, size(jobs)=%d):\n  %s',
      project.pid(),
      jobs.size(),
      String.join(
        '\n  ',
        new Mapped<>({ it.toString() }, ranks)
      )
    )
  }
  int max = new Policy().get('3.absolute-max', 32)
  int count = 0
  long vtime = System.nanoTime()
  String elected = 'not-elected'

  Bans bans = new Bans(project).bootstrap()
  for (String job : jobs) {
    if (orders.contains(job) || reviews.contains(job)) {
      continue
    }
    if (bans.reasons(job).empty) {
      // reporter should be banned first
      continue
    }
    ++count
    String role = wbs.role(job)
    List<String> allogins = roles.findByRole(role)
    List<String> logins = []
    if (deficit) {
      for (String login : allogins) {
        if (!new Rates(project).bootstrap().exists(login)) {
          logins.add(login)
        }
      }
    } else {
      logins.addAll(allogins)
    }
    if (logins.empty) {
      return
    }
    ElectionResult result = new ElectionResult(
      new Election(
        job, logins,
        [
          (wrapped(new VsHardCap(pmo, max)))                                     : -100,
          (wrapped(new VsReputation(pmo, logins)))                               : 4,
          (wrapped(new VsLosers(pmo, new Policy().get('3.low-threshold', -128)))): -100,
          (wrapped(new VsRate(project, logins)))                                 : 2,
          (wrapped(new VsBigDebt(pmo)))                                          : -100,
          (wrapped(new VsNoRoom(pmo)))                                           : role == 'REV' ? 0 : -100,
          (wrapped(new VsOptionsMaxJobs(pmo)))                                   : role == 'REV' ? 0 : -100,
          (wrapped(new VsOptionsMaxRevJobs(pmo)))                                : role == 'REV' ? -100 : 0,
          (wrapped(new VsBanned(project, job)))                                  : -100,
          (wrapped(new VsVacation(pmo)))                                         : -100,
          (wrapped(new VsWorkload(farm, logins)))                                : 1,
          (wrapped(new VsWorkload(farm, project, logins)))                       : 1,
          (wrapped(new VsSpeed(pmo, logins)))                                    : 3,
          (wrapped(new VsBalance(project, farm, logins)))                        : 3,
          (wrapped(new VsRandom()))                                              : 1,
          (wrapped(new VsBlanks(pmo, logins)))                                   : -1,
          (wrapped(new VsNegligence(pmo, logins)))                               : -1,
          (wrapped(new VsVerbosity(pmo, logins)))                                : -1
        ]
      )
    )
    if (result.elected()) {
      elected = job
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
  if (Logger.isInfoEnabled(ltag)) {
    Logger.info(
      ltag,
      'Election was completed for job %s at %d attempt, votes time was %[nano]s',
      elected, count, System.nanoTime() - vtime
    )
  }
}

static Votes wrapped(Votes votes) {
  new VsSafe(new VsMeasured(votes))
}