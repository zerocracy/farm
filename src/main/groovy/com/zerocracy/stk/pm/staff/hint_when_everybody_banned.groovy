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
package com.zerocracy.stk.pm.staff

import com.jcabi.xml.XML
import com.zerocracy.Farm
import com.zerocracy.Par
import com.zerocracy.Project
import com.zerocracy.farm.Assume
import com.zerocracy.claims.ClaimIn
import com.zerocracy.pm.in.Orders
import com.zerocracy.pm.scope.Wbs
import com.zerocracy.pm.staff.Bans
import com.zerocracy.pm.staff.Roles
import com.zerocracy.pmo.Hint
import java.util.concurrent.TimeUnit

def exec(Project project, XML xml) {
  new Assume(project, xml).notPmo()
  new Assume(project, xml).type('Ping hourly')
  ClaimIn claim = new ClaimIn(xml)
  Orders orders = new Orders(farm, project).bootstrap()
  Wbs wbs = new Wbs(project).bootstrap()
  Roles roles = new Roles(project).bootstrap()
  Bans bans = new Bans(project).bootstrap()
  Farm farm = binding.variables.farm
  String arc = roles.findByRole('ARC')[0]
  wbs.iterate().each { job ->
    if (orders.assigned(job)) {
      return
    }
    String role = wbs.role(job)
    boolean available = roles.findByRole(role).any { uid ->
      !bans.exists(job, uid)
    }
    if (available) {
      return
    }
    new Hint(
      farm,
      (int) TimeUnit.DAYS.toSeconds(5L),
      claim.copy()
        .type('Notify job')
        .token("job;${job}")
        .param('mnemo', 'Everybody is banned')
        .param(
          'message',
          new Par(
            '@%s everybody who has role %s is banned at %s;',
            'I won\'t be able to assign anyone automatically;',
            'consider assigning someone manually (as in §19),',
            'or invite more people (as in §51),',
            'or remove the job from the scope (as in §14)'
          ).say(arc, role, job)
        )
    ).postTo(project)
  }
}
