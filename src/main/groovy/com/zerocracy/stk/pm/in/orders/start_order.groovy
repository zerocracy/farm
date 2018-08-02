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
import com.zerocracy.Policy
import com.zerocracy.Project
import com.zerocracy.SoftException
import com.zerocracy.cash.Cash
import com.zerocracy.entry.ClaimsOf
import com.zerocracy.farm.Assume
import com.zerocracy.claims.ClaimIn
import com.zerocracy.pm.cost.Estimates
import com.zerocracy.pm.in.Orders
import com.zerocracy.pm.qa.Reviews
import com.zerocracy.pm.scope.Wbs
import com.zerocracy.pm.staff.Roles
import com.zerocracy.pmo.Agenda
import com.zerocracy.pmo.People

def exec(Project project, XML xml) {
  new Assume(project, xml).notPmo()
  new Assume(project, xml).type('Start order')
  ClaimIn claim = new ClaimIn(xml)
  String job = claim.param('job')
  Reviews reviews = new Reviews(project).bootstrap()
  if (reviews.exists(job)) {
    throw new SoftException(
      new Par(
        'Job %s is pending quality review,',
        'can\'t start a new order'
      ).say(job)
    )
  }
  String login = claim.param('login')
  Farm farm = binding.variables.farm
  int agenda = new Agenda(farm, login).bootstrap().jobs().size()
  if (agenda > new Policy().get('3.absolute-max', 32)) {
    throw new SoftException(
      new Par(
        'User @%s already has %d jobs in the agenda already;',
        'this is way too many;',
        'I won\'t assign any more, sorry, see §3'
      ).say(login)
    )
  }
  new Orders(project).bootstrap().assign(job, login, claim.cid(), claim.created().toInstant())
  String role = new Wbs(project).bootstrap().role(job)
  String msg
  if (role == 'REV') {
    String arc = new Roles(project).bootstrap().findByRole('ARC')[0]
    msg = new Par(
      'This pull request %s is assigned to @%s, here is',
      '[why](/footprint/%s/%s);',
      'the budget is 15 minutes, see §4;',
      'please, read §27 and',
      'when you decide to accept the changes,',
      'inform @%s (the architect) right in this ticket;',
      'if you decide that this PR should not be accepted ever,',
      'also inform the architect;',
      'this [blog post](http://www.yegor256.com/2015/02/09/serious-code-reviewer.html)',
      'will help you understand what is expected from a code reviewer'
    ).say(job, login, project.pid(), claim.cid(), arc)
  } else {
    msg = new Par(
      'The job %s assigned to @%s, here is',
      '[why](/footprint/%s/%s);',
      'the budget is 30 minutes, see §4;',
      'please, read §8 and §9;',
      'if the task is not clear,',
      'read [this](/2015/02/16/it-is-not-a-school.html)',
      'and [this](/2015/01/15/how-to-cut-corners.html)'
    ).say(job, login, project.pid(), claim.cid())
  }
  if (!new Roles(project).bootstrap().hasAnyRole(login)) {
    msg += new Par(
      '; @%s is not a member of this project yet,',
      'but they can request to join, as §1 explains'
    ).say(login)
  }
  if (new People(farm).bootstrap().vacation(login)) {
    msg += new Par(
      '; we should be aware that @%s is on vacation;',
      'this ticket may be delayed'
    ).say(login)
  }
  Cash cash = Cash.ZERO
  Estimates estimates = new Estimates(project).bootstrap()
  if (estimates.exists(job)) {
    msg += new Par('; there will be a monetary reward for this job').say()
    cash = estimates.get(job)
  } else {
    msg += new Par('; there will be no monetary reward for this job').say()
  }
  claim.reply(msg).postTo(new ClaimsOf(farm, project))
  claim.copy()
    .type('Order was given')
    .param('role', role)
    .param('login', login)
    .param('reason', claim.cid())
    .param('estimate', cash)
    .postTo(new ClaimsOf(farm, project))
}
