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
import com.zerocracy.pm.cost.Ledger
import com.zerocracy.pm.in.Orders
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
  String msg = "Job `${job}` assigned to @${login} " +
    " ([profile](http://www.0crat.com/u/${login}}))." +
    ' The budget is [fixed](http://datum.zerocracy.com/pages/policy.html#4)' +
    ' and it is 30 minutes. Please, read the' +
    ' [Policy](http://datum.zerocracy.com/pages/policy.html) and go ahead.'
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
