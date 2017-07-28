/**
 * Copyright (c) 2016-2017 Zerocracy
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
package com.zerocracy.stk.pm.cost

import com.jcabi.xml.XML
import com.zerocracy.farm.Assume
import com.zerocracy.jstk.Project
import com.zerocracy.pm.ClaimIn
import com.zerocracy.pm.ClaimOut
import com.zerocracy.pm.staff.Roles
/**
 * @todo #63:30min We should multiply the payment with the given boost
 *  factor if a boost factor has been set for the task. The boost factor is
 *  defined in an XML document and the schema is in `pm/cost/boosts.xsd`. Default
 *  job size is 15 minutes. In case boost is not specified, the default factor
 *  should be 2, which means default payment is 30. Let's also use the same
 *  boost factor in awarding points in add_award_points.groovy.
 */
def exec(Project project, XML xml) {
  new Assume(project, xml).type('Make payment')
  ClaimIn claim = new ClaimIn(xml)
  String job = claim.param('job')
  String login = claim.param('login')
  String reason = claim.param('reason')
  int minutes = Integer.parseInt(claim.param('minutes'))
  Roles roles = new Roles(project).bootstrap()
  if (!roles.hasAnyRole(login)) {
    return
  }
  // here we pay
  new ClaimOut()
    .type('Payment was made')
    .param('job', job)
    .param('login', login)
    .param('reason', reason)
    .param('minutes', minutes)
    .postTo(project)
}
