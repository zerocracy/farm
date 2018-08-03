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
import com.zerocracy.pm.cost.Rates
import com.zerocracy.pm.staff.Roles
import com.zerocracy.pmo.Hint
import java.util.concurrent.TimeUnit

def exec(Project project, XML xml) {
  new Assume(project, xml).notPmo()
  new Assume(project, xml).type('Ping hourly')
  ClaimIn claim = new ClaimIn(xml)
  Roles roles = new Roles(project).bootstrap()
  int total = roles.findByRole('QA').size()
  if (total > 0) {
    return
  }
  Rates rates = new Rates(project).bootstrap()
  if (!roles.everybody().any { uid -> rates.exists(uid) }) {
    return
  }
  Farm farm = binding.variables.farm
  new Hint(
    farm,
    (int) TimeUnit.DAYS.toSeconds(5L),
    claim.copy()
      .type('Notify project')
      .token("project;${project.pid()}")
      .param('mnemo', 'Deficit of QAs')
      .param(
        'message',
        new Par(
          'There are no QA people in the project;',
          'this is a serious threat to the discipline in the project,',
          'which may lead to financial losses;',
          'we would recommend to add someone to this role, see §42'
        ).say()
      )
  ).postTo(project)
}
