/*
 * Copyright (c) 2016-2019 Zerocracy
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
import com.zerocracy.claims.ClaimIn
import com.zerocracy.entry.ClaimsOf
import com.zerocracy.farm.Assume
import com.zerocracy.pm.staff.Roles

/**
 * Remind architects to send a report to PO
 * about situation in the project.
 */
def exec(Project project, XML xml) {
  new Assume(project, xml).notPmo()
  new Assume(project, xml).type('Ping 2weeks')
  Farm farm = binding.variables.farm
  ClaimIn claim = new ClaimIn(xml)
  Roles roles = new Roles(project).bootstrap()
  roles.findByRole('ARC').each { arc ->
    claim.copy()
      .type('Notify user')
      .token("user;$arc")
      .param(
      'message',
        new Par(
          farm,
          'Hey, don\'t forget to send a regular bi-weekly report about',
          'the situation in the project %s to its Product Owner (PO) by email;',
          'see this blog post for details:',
          ''.join(
            '[__Software Architect Responsibilities__]',
            '(https://www.yegor256.com/2015/05/11/software-architect-responsibilities.html);'
          ),
          'don\'t forget to CC `pmo@zerocracy.com`'
        ).say(project.pid())
    ).postTo(new ClaimsOf(farm, project))
  }
}
