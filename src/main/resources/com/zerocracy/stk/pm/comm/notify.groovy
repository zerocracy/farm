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
package com.zerocracy.stk.pm.comm

import com.jcabi.xml.XML
import com.zerocracy.Farm
import com.zerocracy.farm.Assume
import com.zerocracy.Project
import com.zerocracy.pm.ClaimIn

def exec(Project project, XML xml) {
  new Assume(project, xml).type('Notify')
  ClaimIn claim = new ClaimIn(xml)
  String[] parts = claim.token().split(';', 2)
  if (parts[0] == 'slack') {
    claim.copy()
      .type('Notify in Slack')
      .postTo(project)
  } else if (parts[0] == 'telegram') {
    claim.copy()
      .type('Notify in Telegram')
      .postTo(project)
  } else if (parts[0] == 'github') {
    claim.copy()
      .type('Notify in GitHub')
      .postTo(project)
  } else if (parts[0] == 'job') {
    claim.copy()
      .type('Notify job')
      .postTo(project)
  } else if (parts[0] == 'test') {
    claim.copy()
      .type('Notify test')
      .postTo(project)
  } else if (parts[0] == 'project') {
    String pid = parts[1]
    if (project.pid() != 'PMO' && pid != project.pid()) {
      throw new IllegalStateException(
        String.format(
          'You can\'t notify another project %s from %s',
          pid, project.pid()
        )
      )
    }
    Farm farm = binding.variables.farm
    claim.copy()
      .type('Notify project')
      .postTo(farm.find("@id='${pid}'")[0])
  } else {
    throw new IllegalStateException(
      String.format(
        'I don\'t know how to notify "%s" in %s: "%s"',
        parts[0], project, claim.param('message')
      )
    )
  }
}
