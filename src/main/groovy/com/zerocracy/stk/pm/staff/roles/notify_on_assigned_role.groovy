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
import com.zerocracy.pm.cost.Rates

def exec(Project project, XML xml) {
  new Assume(project, xml).notPmo()
  new Assume(project, xml).type('Role was assigned')
  ClaimIn claim = new ClaimIn(xml)
  String login = claim.param('login')
  String role = claim.param('role')
  Farm farm = binding.variables.farm
  String msg = new Par(
    farm,
    'You just got role %s in the %s project; '
  ).say(role, project.pid())
  Rates rates = new Rates(project).bootstrap()
  if (rates.exists(login)) {
    msg += new Par('your rate is %s').say(rates.rate(login))
  } else {
    msg += 'you work for free'
  }
  if (role == 'ARC') {
    msg += new Par(
      '; since your new role is the architect, I would recommend you to read',
      'these two blog posts:',
      '[Three Things I Expect From',
      'a Software Architect](http://www.yegor256.com/2015/05/11/software-architect-responsibilities.html)',
      ' and ',
      '[Two Instruments of',
      'a Software Architect](http://www.yegor256.com/2015/05/13/two-instruments-of-software-architect.html);',
      'if the project just starts, these two article will also be helpful:',
      '[Project Lifecycle in',
      'Zerocracy](http://www.yegor256.com/2014/10/06/software-project-lifecycle.html)',
      ' and ',
      '[Nine Steps to Start',
      'a Software Project](http://www.yegor256.com/2015/08/04/nine-steps-start-software-project.html)'
    ).say()
  }
  claim.copy()
    .type('Notify user')
    .token("user;${login}")
    .param('message', msg)
    .postTo(new ClaimsOf(farm, project))
}
