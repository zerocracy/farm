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
package com.zerocracy.stk.pm.comm

import com.jcabi.xml.XML
import com.zerocracy.Farm
import com.zerocracy.Project
import com.zerocracy.entry.ClaimsOf
import com.zerocracy.farm.Assume
import com.zerocracy.claims.ClaimIn
/**
 * Stakeholder for notifications. It can understand what channel
 * to use to notify by token (claim parameter)
 * and produce a more concrete notification claim.
 *
 * @param project Any project
 * @param xml Claim
 * @todo #1071:30min Let's allow notification via Viber. If we receive a Notify
 *  claim with a 'viber' prefixed token, we copy the claim to a new claim,
 *  "Notify in Viber". Then, create a stakeholder that will handle the claim
 *  and send the actual message to the user. Also create a mock/fake VbBot
 *  implementation that we can use for stakeholder testing (may require
 *  extracting an interface for VbBot).
 */
def exec(Project project, XML xml) {
  new Assume(project, xml).type('Notify')
  ClaimIn claim = new ClaimIn(xml)
  String[] parts = claim.token().split(';', 2)
  Farm farm = binding.variables.farm
  if (parts[0] == 'slack') {
    claim.copy()
      .type('Notify in Slack')
      .postTo(new ClaimsOf(farm, project))
  } else if (parts[0] == 'telegram') {
    claim.copy()
      .type('Notify in Telegram')
      .postTo(new ClaimsOf(farm, project))
  } else if (parts[0] == 'github') {
    claim.copy()
      .type('Notify in GitHub')
      .postTo(new ClaimsOf(farm, project))
  } else if (parts[0] == 'job') {
    claim.copy()
      .type('Notify job')
      .postTo(new ClaimsOf(farm, project))
  } else if (parts[0] == 'test') {
    claim.copy()
      .type('Notify test')
      .postTo(new ClaimsOf(farm, project))
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
    Project notify = farm.find("@id='${pid}'")[0]
    claim.copy()
      .type('Notify project')
      .postTo(new ClaimsOf(farm, notify))
  } else {
    throw new IllegalStateException(
      String.format(
        'I don\'t know how to notify "%s" in %s: "%s"',
        parts[0], project, claim.param('message')
      )
    )
  }
}
