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
package com.zerocracy.stk.pm.in.orders

import com.jcabi.xml.XML
import com.zerocracy.farm.Assume
import com.zerocracy.Project
import com.zerocracy.pm.ClaimOut
import com.zerocracy.pm.in.Orders
import com.zerocracy.pm.scope.Wbs
import com.zerocracy.pm.staff.Elections

def exec(Project project, XML xml) {
  new Assume(project, xml).notPmo()
  new Assume(project, xml).type('Ping')
  Wbs wbs = new Wbs(project).bootstrap()
  Orders orders = new Orders(project).bootstrap()
  Elections elections = new Elections(project).bootstrap()
  for (String job : wbs.iterate()) {
    if (!orders.assigned(job) && elections.elected(job)) {
      def winner = elections.winner(job)
      def reason = elections.reason(job)
      new ClaimOut()
        .type('Start order')
        .token("job;${job}")
        .param('job', job)
        .param('login', winner)
        .param('reason', reason)
        .param('public', true)
        .postTo(project)
    }
  }
}

