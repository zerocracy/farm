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
package com.zerocracy.stk.pm

import com.jcabi.xml.XML
import com.zerocracy.Farm
import com.zerocracy.Par
import com.zerocracy.Policy
import com.zerocracy.Project
import com.zerocracy.SoftException
import com.zerocracy.entry.ClaimsOf
import com.zerocracy.farm.Assume
import com.zerocracy.claims.ClaimIn
import com.zerocracy.pm.staff.Roles
import com.zerocracy.pmo.Catalog
import com.zerocracy.pmo.Exam

def exec(Project project, XML xml) {
  new Assume(project, xml).notPmo()
  new Assume(project, xml).type('Bootstrap')
  Farm farm = binding.variables.farm
  Roles roles = new Roles(project).bootstrap()
  ClaimIn claim = new ClaimIn(xml)
  String author = claim.author()
  new Exam(farm, author).min('12.min', 1024)
  if (roles.hasRole(author, 'PO')) {
    throw new SoftException(
      new Par(
        'This project is already ready to go;',
        'you are a product owner, see §12'
      ).say()
    )
  }
  if (!roles.findByRole('PO').empty) {
    throw new SoftException(
      new Par(
        'This project already has a product owner;',
        'no need to bootstrap again, see §12'
      ).say()
    )
  }
  roles.assign(author, 'PO')
  roles.assign(author, 'ARC')
  Catalog catalog = new Catalog(farm).bootstrap()
  catalog.link(project.pid(), 'slack', project.pid())
  catalog.adviser(project.pid(), claim.author())
  claim.copy()
    .type('Adviser was updated')
    .postTo(new ClaimsOf(farm, project))
  claim.copy()
    .type('Role was assigned')
    .param('login', author)
    .param('role', 'PO')
    .postTo(new ClaimsOf(farm, project))
  claim.copy()
    .type('Role was assigned')
    .param('login', author)
    .param('role', 'ARC')
    .postTo(new ClaimsOf(farm, project))
  claim.copy()
    .type('Make payment')
    .param('login', author)
    .param('job', 'none')
    .param('minutes', -new Policy().get('12.price', 256))
    .param('reason', new Par('Project %s was bootstrapped').say(project.pid()))
    .postTo(new ClaimsOf(farm, project))
  if (claim.hasParam('channel')) {
    String chnl = claim.param('channel')
    if (chnl.length() > 2) {
      claim.copy()
        .type('Set title')
        .param('title', chnl)
        .postTo(new ClaimsOf(farm, project))
    }
  }
  claim.reply(
    new Par(
      farm,
      'I\'m ready to manage the %s project;',
      'when you\'re ready, you can start giving me commands,',
      'always prefixing your messages with my name;',
      'all project artifacts are [here](/p/%1$s);',
      'start with linking your project with GitHub repositories, as explained in §17;',
      'I just assigned you to both ARC and PO roles',
      'read these two articles before you start:',
      '[Project Lifecycle in Zerocracy](http://www.yegor256.com/2014/10/06/software-project-lifecycle.html)',
      ' and ',
      '[Nine Steps to Start',
      'a Software Project](http://www.yegor256.com/2015/08/04/nine-steps-start-software-project.html)'
    ).say(project.pid())
  ).postTo(new ClaimsOf(farm, project))
  claim.copy().type('Notify PMO').param(
    'message', new Par(
      'We just bootstrapped @%s by @%s'
    ).say(project.pid(), author)
  ).postTo(new ClaimsOf(farm, project))
}
