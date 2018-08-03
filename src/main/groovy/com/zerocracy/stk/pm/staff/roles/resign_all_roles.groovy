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

import com.jcabi.xml.XML
import com.zerocracy.Farm
import com.zerocracy.Par
import com.zerocracy.Project
import com.zerocracy.entry.ClaimsOf
import com.zerocracy.farm.Assume
import com.zerocracy.claims.ClaimIn
import com.zerocracy.pm.staff.Roles

def exec(Project project, XML xml) {
  new Assume(project, xml).notPmo()
  new Assume(project, xml).type('Resign all roles')
  ClaimIn claim = new ClaimIn(xml)
  String login = claim.param('login')
  Roles roles = new Roles(project).bootstrap()
  Farm farm = binding.variables.farm
  claim.reply(
    new Par('All roles were resigned from @%s in %s').say(
      login, project.pid()
    )
  ).postTo(new ClaimsOf(farm, project))
  roles.allRoles(login).each { role ->
    roles.resign(login, role)
    claim.copy()
      .type('Role was resigned')
      .param('login', login)
      .param('role', role)
      .postTo(new ClaimsOf(farm, project))
  }
  claim.copy()
    .type('Notify project')
    .param(
      'message',
      new Par(
        'Project member @%s was resigned from all project roles: %s',
      ).say(login, claim.param('reason'))
    )
    .postTo(new ClaimsOf(farm, project))
}
