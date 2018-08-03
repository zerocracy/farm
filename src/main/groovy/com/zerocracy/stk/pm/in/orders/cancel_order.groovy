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
package com.zerocracy.stk.pm.in.orders

import com.jcabi.xml.XML
import com.zerocracy.Farm
import com.zerocracy.Par
import com.zerocracy.Project
import com.zerocracy.SoftException
import com.zerocracy.entry.ClaimsOf
import com.zerocracy.farm.Assume
import com.zerocracy.claims.ClaimIn
import com.zerocracy.pm.in.Orders
import com.zerocracy.pm.staff.Roles

def exec(Project project, XML xml) {
  new Assume(project, xml).notPmo()
  new Assume(project, xml).type('Cancel order')
  ClaimIn claim = new ClaimIn(xml)
  String job = claim.param('job')
  Orders orders = new Orders(project).bootstrap()
  String performer = orders.performer(job)
  Roles roles = new Roles(project).bootstrap()
  if (claim.hasAuthor() && !roles.hasRole(claim.author(), 'PO', 'ARC')
    && claim.author() != performer) {
    throw new SoftException(
      new Par(
        'The job %s is assigned to @%s,',
        'you cannot resign, since you are not a PO or ARC',
      ).say(job, performer)
    )
  }
  orders.resign(job)
  Farm farm = binding.variables.farm
  String reason
  if (claim.hasParam('reason')) {
    reason = claim.param('reason')
  } else {
    reason = 'Order was cancelled'
  }
  claim.reply(
    new Par(
      'The user @%s resigned from %s, please stop working. Reason for job resignation: %s',
    ).say(performer, job, reason)
  ).postTo(new ClaimsOf(farm, project))
  claim.copy()
    .type('Order was canceled')
    .param('voluntarily', claim.hasAuthor() && claim.author() == performer)
    .param('login', performer)
    .postTo(new ClaimsOf(farm, project))
}
