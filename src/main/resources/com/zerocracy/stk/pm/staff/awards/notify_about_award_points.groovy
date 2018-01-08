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
package com.zerocracy.stk.pm.staff.awards

import com.jcabi.xml.XML
import com.zerocracy.farm.Assume
import com.zerocracy.jstk.Project
import com.zerocracy.pm.ClaimIn
import com.zerocracy.pm.ClaimOut
import com.zerocracy.pmo.Awards

def exec(Project project, XML xml) {
  new Assume(project, xml).notPmo()
  new Assume(project, xml).type('Award points were added')
  ClaimIn claim = new ClaimIn(xml)
  String job = claim.param('job')
  String login = claim.param('login')
  Integer points = Integer.parseInt(claim.param('points'))
  Awards awards = new Awards(project, login).bootstrap()
  String reason = claim.param('reason')
  new ClaimOut()
    .type('Notify user')
    .token("user;${login}")
    .param(
      'message',
      "You got ${points} [points](http://datum.zerocracy.com/pages/policy.html#18) in `${job}`" +
      " ([${project.pid()}](http://www.0crat.com/p/${project.pid()}))," +
      ' your total is ' +
      " [${awards.total()}](http://www.0crat.com/u/${login}/awards): ${reason}."
    )
    .postTo(project)
  new ClaimOut()
    .type('Notify job')
    .token("job;${job}")
    .param(
      'message',
      String.format(
        '%s: %+d points just awarded to @%s, total is'
        + ' [%+d](http://www.0crat.com/u/%s).',
        reason, points, login, awards.total(), login
      )
    )
    .postTo(project)
}
