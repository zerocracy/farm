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
package com.zerocracy.stk.pm.staff.roles

import com.jcabi.github.Coordinates
import com.jcabi.github.Github
import com.jcabi.github.Repo
import com.jcabi.xml.XML
import com.zerocracy.Farm
import com.zerocracy.Par
import com.zerocracy.Project
import com.zerocracy.SoftException
import com.zerocracy.cash.Cash
import com.zerocracy.entry.ClaimsOf
import com.zerocracy.entry.ExtGithub
import com.zerocracy.farm.Assume
import com.zerocracy.claims.ClaimIn
import com.zerocracy.pm.cost.Rates
import com.zerocracy.pm.staff.Roles
import com.zerocracy.pmo.Catalog
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
  if (people.wallet(login).empty && claim.hasParam('rate')) {
    throw new SoftException(
      new Par(
        '@%s doesn\'t have a payment method configured yet,',
        'we won\'t be able to pay them;',
        'the rate can\'t be set.'
      ).say(login)
    )
  }
  Github github = new ExtGithub(farm).value()
  for (String link: new Catalog(farm).links(project.pid(), 'github')) {
    Repo.Smart repo = new Repo.Smart(github.repos().get(new Coordinates.Simple(link)))
    if (repo.private && !repo.collaborators().isCollaborator(login)) {
      throw new SoftException(
        new Par(
          '@%s doesn\'t have an access in your repositories,',
          'we won\'t be able to assign tasks'
        ).say(login)
      )
    }
  }
  Roles roles = new Roles(project).bootstrap()
  Rates rates = new Rates(project).bootstrap()
  String msg
  if (roles.hasRole(login, role)) {
    msg = new Par(
      'Role %s was already assigned to @%s; '
    ).say(role, login)
  } else {
    String text = 'Role %s was successfully assigned to @%s, see [full list](/a/%s?a=pm/staff/roles) of roles; '
    if (role == 'DEV' && !roles.hasRole(login, 'ARC')) {
      claim.copy()
        .type('Assign Role')
        .param('role', 'REV')
        .postTo(new ClaimsOf(farm, project))
    } else if (role == 'REV') {
        text = text + 'If you don\'t want this user as \'REV\', use `resign REV` command; '
    }
    roles.assign(login, role)
    msg = new Par(
        text
    ).say(role, login, project.pid())
    claim.copy()
      .type('Role was assigned')
      .param('role', role)
      .postTo(new ClaimsOf(farm, project))
  }
  if (claim.hasParam('rate')) {
    Cash rate = new Cash.S(claim.param('rate'))
    claim.copy()
      .type('Change user rate')
      .param('rate', rate)
      .postTo(new ClaimsOf(farm, project))
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
  if (claim.hasParam('vesting')) {
    Cash rate = new Cash.S(claim.param('vesting'))
    claim.copy()
      .type('Change user vesting rate')
      .param('rate', rate)
      .postTo(new ClaimsOf(farm, project))
  }
  claim.reply(msg).postTo(new ClaimsOf(farm, project))
}
