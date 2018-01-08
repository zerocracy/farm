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
package com.zerocracy.stk.pmo.profile

import com.jcabi.xml.XML
import com.zerocracy.farm.Assume
import com.zerocracy.jstk.Project
import com.zerocracy.pm.ClaimIn
import com.zerocracy.pm.ClaimOut
import com.zerocracy.pmo.People

def exec(Project project, XML xml) {
  new Assume(project, xml).isPmo()
  new Assume(project, xml).type('Invite a friend')
  ClaimIn claim = new ClaimIn(xml)
  String login = claim.param('login')
  People people = new People(project).bootstrap()
  people.invite(login, claim.author())
  claim.reply(
    String.format(
      'Thanks, `@%s` can now work with us, and you are the mentor,' +
      ' see [§1](http://datum.zerocracy.com/pages/policy.html#1).',
      login
    )
  ).postTo(project)
  new ClaimOut()
    .type('Notify user')
    .token("user;${login}")
    .param(
      'message',
      "You have been invited to Zerocracy by @${claim.author()}," +
      ' as required in [§1](http://datum.zerocracy.com/pages/policy.html#1).' +
      ' You can now apply to the projects, see' +
      ' [§2](http://datum.zerocracy.com/pages/policy.html#1).'
    )
    .postTo(project)
}
