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
package com.zerocracy.stk.pm

import com.jcabi.xml.XML
import com.zerocracy.Farm
import com.zerocracy.Par
import com.zerocracy.Project
import com.zerocracy.SoftException
import com.zerocracy.farm.Assume
import com.zerocracy.pm.ClaimIn
import com.zerocracy.pm.ClaimOut
import com.zerocracy.pm.staff.Roles
import com.zerocracy.pmo.Catalog

def exec(Project project, XML xml) {
  new Assume(project, xml).notPmo()
  new Assume(project, xml).type('Bootstrap')
  Farm farm = binding.variables.farm
  Roles roles = new Roles(project).bootstrap()
  ClaimIn claim = new ClaimIn(xml)
  String author = claim.author()
  String role = 'PO'
  if (roles.empty) {
    roles.assign(author, role)
    roles.assign(author, 'ARC')
    new Catalog(farm).bootstrap().link(
      project.pid(),
      'slack',
      project.pid()
    )
    new ClaimOut()
      .type('Role was assigned')
      .param('login', author)
      .param('role', role)
      .postTo(project)
    new ClaimOut()
      .type('Role was assigned')
      .param('login', author)
      .param('role', 'ARC')
      .postTo(project)
    if (claim.hasParam('channel')) {
      claim.copy()
        .type('Set title')
        .param('title', claim.param('channel'))
        .param('project', project.pid())
        .postTo(project)
    }
    if (claim.hasToken()) {
      claim.reply(
        new Par(
          farm,
          'I\'m ready to manage the %s project.',
          'When you\'re ready, you can start giving me commands,',
          'always prefixing your messages with my name.',
          'All project artifacts are [here](/p/%1$s).',
          'Start with linking your project with GitHub repositories,',
          'as explained in §17. I just assigned you to both ARC and PO',
          'roles.'
        ).say(project.pid())
      ).postTo(project)
      new ClaimOut().type('Notify user').token('user;yegor256').param(
        'message', new Par(
          'We just bootstrapped @%s by @%s'
        ).say(project.pid(), author)
      ).param('cause', claim.cid()).postTo(project)
    }
  } else {
    if (roles.hasRole(author, role)) {
      throw new SoftException(
        'This project is already ready to go'
      )
    }
    throw new SoftException(
      'You are not a product owner here, cannot bootstrap it'
    )
  }
}
