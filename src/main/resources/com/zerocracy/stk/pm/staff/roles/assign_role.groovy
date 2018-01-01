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
package com.zerocracy.stk.pm.staff.roles

import com.jcabi.xml.XML
import com.zerocracy.farm.Assume
import com.zerocracy.jstk.Project
import com.zerocracy.jstk.SoftException
import com.zerocracy.jstk.cash.Cash
import com.zerocracy.pm.ClaimIn
import com.zerocracy.pm.ClaimOut
import com.zerocracy.pm.cost.Rates
import com.zerocracy.pm.staff.Roles
import com.zerocracy.pmo.People

def exec(Project project, XML xml) {
  new Assume(project, xml).notPmo()
  new Assume(project, xml).type('Assign role')
  new Assume(project, xml).roles('ARC', 'PO')
  ClaimIn claim = new ClaimIn(xml)
  String login = claim.param('login')
  People people = new People(project).bootstrap()
  if (!people.hasMentor(login)) {
    throw new SoftException(
      "Assignee @${login} must be a registered person."
    )
  }
  String role = claim.param('role')
  Roles roles = new Roles(project).bootstrap()
  Rates rates = new Rates(project).bootstrap()
  String msg
  if (roles.hasRole(login, role)) {
    msg = "Role `${role}` was already assigned to @${login}. "
  } else {
    roles.assign(login, role)
    msg = "Role `${role}` was successfully assigned to @${login}," +
      " see [full list](http://www.0crat.com/a/${project}?a=pm/staff/roles)" +
      ' of roles. '
    new ClaimOut()
      .type('Role was assigned')
      .param('login', login)
      .param('role', role)
      .postTo(project)
  }
  if (claim.hasParam('rate')) {
    Cash rate = new Cash.S(claim.param('rate'))
    claim.copy()
      .type('Change user rate')
      .param('login', login)
      .param('rate', rate)
      .postTo(project)
  } else {
    if (rates.exists(login)) {
      msg += "Hourly rate of @${login} is ${rates.rate(login)}." +
        " To change the rate, say `assign ${role} ${login} \$25`, for example."
    } else {
      msg += "Hourly rate of @${login} is not set," +
        ' the user will receive no money for task completion.' +
        " If you want to assign an hourly rate, say `assign ${role} ${login} \$25`," +
        ' for example.'
    }
  }
  claim.reply(msg).postTo(project)
}
