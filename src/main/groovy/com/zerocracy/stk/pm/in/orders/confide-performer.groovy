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

import com.zerocracy.jstk.Project
import com.zerocracy.pm.ClaimOut
import com.zerocracy.pm.hr.Roles
import com.zerocracy.pm.in.Orders
import com.zerocracy.pm.scope.Wbs

assume.type('Ping').exact()

final Wbs wbs = new Wbs(project)
final Orders orders = new Orders(project)
for (String job : wbs.iterate()) {
  if (!orders.assigned(job)) {
    this.assign(project, job)
  }
}

static void assign(Project project, String job) {
  final Roles roles = new Roles(project)
  final List<String> logins = roles.findByRole('DEV')
  Collections.shuffle(logins)
  if (!logins.empty) {
    final String login = logins[0]
    new ClaimOut()
      .type('Start order')
      .param('job', job)
      .param('login', login)
      .postTo(project)
    new ClaimOut()
      .type('Performer was confided')
      .param('login', login)
      .param('job', job)
      .param('reason', 'Because I love you')
      .postTo(project)
  }
}
