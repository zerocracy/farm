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
package com.zerocracy.stk.pmo.awards

import com.jcabi.xml.XML
import com.zerocracy.Farm
import com.zerocracy.Par
import com.zerocracy.Policy
import com.zerocracy.Project
import com.zerocracy.entry.ClaimsOf
import com.zerocracy.farm.Assume
import com.zerocracy.claims.ClaimIn
import com.zerocracy.pm.staff.Roles
import com.zerocracy.pmo.Awards
import com.zerocracy.pmo.People
import com.zerocracy.pmo.Pmo

def exec(Project project, XML xml) {
  new Assume(project, xml).notPmo()
  new Assume(project, xml).type('Award points were added')
  ClaimIn claim = new ClaimIn(xml)
  String login = claim.param('login')
  Farm farm = binding.variables.farm
  Awards awards = new Awards(farm, login).bootstrap()
  Integer current = awards.total()
  if (current > new Policy().get('44.threshold', -256)) {
    return
  }
  People people = new People(farm).bootstrap()
  if (new Roles(new Pmo(farm)).bootstrap().hasAnyRole(login)
    || people.mentor(login) == '0crat') {
    claim.copy()
      .type('Notify user')
      .token("user;${login}")
      .param(
        'message',
        new Par(
          'You are a very respected person, but your reputation is very low: %d;',
          'please, do something or I will take some disciplinary actions, see §44'
        ).say(current)
      ).postTo(new ClaimsOf(farm, project))
    return
  }
  people.breakup(login)
  claim.copy()
    .type('Notify user')
    .token("user;${login}")
    .param(
      'message',
      new Par(
        'Your reputation became too low,',
        'you have been disconnected from your mentor as in §44'
      ).say()
    ).postTo(new ClaimsOf(farm, project))
  String job = claim.param('job')
  String reason = new Par(
    'The score of @%s %d is too low and will be reset'
  ).say(login, current)
  Integer points = -current
  awards.add(project, points, job, new Par.ToText(reason).toString())
  claim.copy()
    .type('Award points were added')
    .param('points', points)
    .param('reason', reason)
    .postTo(new ClaimsOf(farm, project))
}
