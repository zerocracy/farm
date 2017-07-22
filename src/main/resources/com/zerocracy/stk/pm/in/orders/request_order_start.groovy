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
package com.zerocracy.stk.pm.in.orders

import com.jcabi.xml.XML
import com.zerocracy.farm.Assume
import com.zerocracy.jstk.Project
import com.zerocracy.pm.ClaimIn
import com.zerocracy.pm.ClaimOut
import com.zerocracy.pm.in.Orders
import com.zerocracy.pm.scope.Wbs

def exec(Project project, XML xml) {
  new Assume(project, xml).type('Request order start')
  new Assume(project, xml).roles('ARC', 'PO')
  ClaimIn claim = new ClaimIn(xml)
  String login = claim.param('login').replaceAll('^@', '')
  String job = claim.param('job')
  if ('me' == login) {
    login = claim.author()
  }
  Wbs wbs = new Wbs(project).bootstrap()
  if (!wbs.exists(job)) {
    wbs.add(job)
    new ClaimOut()
      .type('Job was added to WBS')
      .param('job', job)
      .postTo(project)
  }
  Orders orders = new Orders(project).bootstrap()
  if (orders.assigned(job)) {
    String performer = orders.performer(job)
    if (login == performer) {
      claim.reply(
        String.format(
          'Job `%s` is already assigned to @%s.',
          job, login
        )
      ).postTo(project)
      return
    }
    orders.resign(job)
    new ClaimOut()
      .type('Order was canceled')
      .param('job', job)
      .param('login', performer)
      .postTo(project)
  }
  claim.copy()
    .type('Start order')
    .param('login', login)
    .param('reason', 'Per their request.')
    .postTo(project)
}
