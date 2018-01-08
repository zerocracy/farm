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
package com.zerocracy.stk.pm.in.orders

import com.jcabi.xml.XML
import com.zerocracy.farm.Assume
import com.zerocracy.jstk.Project
import com.zerocracy.pm.ClaimIn
import com.zerocracy.pm.ClaimOut
import com.zerocracy.pm.cost.Boosts
import com.zerocracy.pm.cost.Ledger
import com.zerocracy.pm.in.Orders
import com.zerocracy.pm.scope.Wbs
import com.zerocracy.pm.staff.Roles
import com.zerocracy.pmo.People

def exec(Project project, XML xml) {
  new Assume(project, xml).notPmo()
  new Assume(project, xml).type('Start order')
  ClaimIn claim = new ClaimIn(xml)
  String job = claim.param('job')
  String login = claim.param('login')
  String reason = claim.param('reason')
  Orders orders = new Orders(project).bootstrap()
  if (new Ledger(project).bootstrap().deficit()) {
    return
  }
  orders.assign(job, login, reason)
  String role = new Wbs(project).bootstrap().role(job)
  String msg
  if (role == 'REV') {
    new Boosts(project).boost(job, 1)
    String arc = new Roles(project).bootstrap().findByRole('ARC')[0]
    msg = "This PR `${job}` assigned to @${login} " +
      " ([profile](http://www.0crat.com/u/${login}}))." +
      ' The budget is [fixed](http://datum.zerocracy.com/pages/policy.html#4)' +
      ' and it is 15 minutes. Please, read' +
      ' [§27](http://datum.zerocracy.com/pages/policy.html#27)' +
      ' and [§10](http://datum.zerocracy.com/pages/policy.html#10).'
      " If and when you decide to accept the changes, inform @${arc} right in this ticket."
  } else {
    msg = "Job `${job}` assigned to @${login} " +
      " ([profile](http://www.0crat.com/u/${login}}))." +
      ' The budget is [fixed](http://datum.zerocracy.com/pages/policy.html#4)' +
      ' and it is 30 minutes. Please, read' +
      ' [§4](http://datum.zerocracy.com/pages/policy.html#4),' +
      ' [§8](http://datum.zerocracy.com/pages/policy.html#8),' +
      ' and [§9](http://datum.zerocracy.com/pages/policy.html#9).' +
      ' If the task is not clear, read [this](http://www.yegor256.com/2015/02/16/it-is-not-a-school.html)' +
      ' and [this](http://www.yegor256.com/2015/01/15/how-to-cut-corners.html).'
  }
  if (!new Roles(project).bootstrap().hasAnyRole(login)) {
    msg += " @${login} is not a member of this project yet," +
      ' but can request to join.' +
      " @${login} check your" +
      " [Zerocracy profile](http://www.0crat.com/u/${login})" +
      ' and follow the instructions.'
  }
  if (new People(project).bootstrap().vacation(login)) {
    msg += "\n\n@${claim.author()}, hey! You should be aware that " +
      "@${login} is on vacation! This ticket may be delayed."
  }
  claim.reply(msg).postTo(project)
  new ClaimOut()
    .type('Order was given')
    .param('job', job)
    .param('login', login)
    .param('reason', reason)
    .postTo(project)
}
