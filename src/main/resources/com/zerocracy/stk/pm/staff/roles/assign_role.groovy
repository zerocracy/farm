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
import com.zerocracy.Farm
import com.zerocracy.Par
import com.zerocracy.Project
import com.zerocracy.SoftException
import com.zerocracy.cash.Cash
import com.zerocracy.farm.Assume
import com.zerocracy.pm.ClaimIn
import com.zerocracy.pm.cost.Rates
import com.zerocracy.pm.staff.Roles
import com.zerocracy.pmo.People

def exec(Project project, XML xml) {
  new Assume(project, xml).notPmo()
  new Assume(project, xml).type('Assign role')
  new Assume(project, xml).roles('ARC', 'PO')
  ClaimIn claim = new ClaimIn(xml)
  String login = claim.param('login')
  Farm farm = binding.variables.farm
  People people = new People(farm).bootstrap()
  String role = claim.param('role')
  if (!people.hasMentor(login) && role != 'PO') {
    throw new SoftException(
      new Par('Assignee @%s must be invited').say(login)
    )
  }
  Roles roles = new Roles(project).bootstrap()
  Rates rates = new Rates(project).bootstrap()
  String msg
  if (roles.hasRole(login, role)) {
    msg = new Par(
      'Role %s was already assigned to @%s; '
    ).say(role, login)
  } else {
    roles.assign(login, role)
    msg = new Par(
      'Role %s was successfully assigned to @%s,',
      'see [full list](/a/%s?a=pm/staff/roles) of roles; '
    ).say(role, login, project.pid())
    claim.copy()
      .type('Role was assigned')
      .param('role', role)
      .postTo(project)
  }
  if (claim.hasParam('rate')) {
    Cash rate = new Cash.S(claim.param('rate'))
    claim.copy()
      .type('Change user rate')
      .param('rate', rate)
      .postTo(project)
  } else {
    if (rates.exists(login)) {
      msg += new Par(
        'hourly rate of @%s is %s;',
        'to change the rate, say `assign XXX %1$s \$25`, for example,',
        'where XXX must be %s or any other role, as in §13'
      ).say(login, rates.rate(login), role)
    } else {
      msg += new Par(
        'hourly rate of @%s is not set,',
        'the user will receive no money for task completion;',
        'if you want to assign an hourly rate, say `assign %s %1$s \$25`,',
        'for example'
      ).say(login, role)
    }
  }
  claim.reply(msg).postTo(project)
}
