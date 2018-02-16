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
import com.zerocracy.Par
import com.zerocracy.farm.Assume
import com.zerocracy.Project
import com.zerocracy.pm.ClaimIn
import com.zerocracy.pm.ClaimOut
import com.zerocracy.pm.staff.Roles
import com.zerocracy.pmo.Projects

def exec(Project project, XML xml) {
  new Assume(project, xml).notPmo()
  new Assume(project, xml).type('Resign all roles')
  ClaimIn claim = new ClaimIn(xml)
  String login = claim.param('login')
  new Roles(project).bootstrap().resign(login)
  new Projects(project, login).bootstrap().remove(project.pid())
  if (claim.hasToken()) {
    claim.reply(
      new Par('All roles resigned from @%s').say(login)
    ).postTo(project)
  }
  new ClaimOut()
    .type('All roles were resigned')
    .param('cause', claim.cid())
    .param('login', login)
    .postTo(project)
}
