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
package com.zerocracy.stk.pmo.awards

import com.jcabi.xml.XML
import com.zerocracy.Farm
import com.zerocracy.Par
import com.zerocracy.Policy
import com.zerocracy.Project
import com.zerocracy.farm.Assume
import com.zerocracy.pm.ClaimIn
import com.zerocracy.pmo.Awards
import com.zerocracy.pmo.People

def exec(Project project, XML xml) {
  new Assume(project, xml).notPmo()
  new Assume(project, xml).type('Award points were added')
  ClaimIn claim = new ClaimIn(xml)
  String login = claim.param('login')
  Farm farm = binding.variables.farm
  Awards awards = new Awards(farm, login).bootstrap()
  Integer current = awards.total()
  if (current <= new Policy().get('44.threshold', -256)) {
    People people = new People(farm).bootstrap()
    people.breakup(login)
    claim.copy()
      .token("user;${login}")
      .param(
        'message',
        new Par('Your reputation becomes too low, so you have been disconnected from your mentor as in §44').say()
      ).postTo(project)
    String job = claim.param('job')
    String reason = new Par(
      'The score of @%s %d is too low and will be reset'
    ).say(login, current)
    Integer points = -current
    awards.add(project, points, job, new Par.ToText(reason).toString())
    claim.copy()
      .type('Award points were added')
      .param('login', login)
      .param('points', points)
      .param('reason', reason)
      .postTo(project)
  }
}
