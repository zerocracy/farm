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

import com.jcabi.log.Logger
import com.jcabi.xml.XML
import com.zerocracy.Farm
import com.zerocracy.Par
import com.zerocracy.Project
import com.zerocracy.claims.ClaimIn
import com.zerocracy.entry.ClaimsOf
import com.zerocracy.farm.Assume
import com.zerocracy.pmo.Awards
import com.zerocracy.pmo.Options
import com.zerocracy.pmo.People
import com.zerocracy.pmo.Pmo

/**
 * Notify all users. Claim sender can specify minimum required reputation
 * to receive notification. Also users on vacation are ignored.
 *
 * @param project Any project
 * @param xml Claim
 */
def exec(Project project, XML xml) {
  new Assume(project, xml).type('Notify all')
  ClaimIn claim = new ClaimIn(xml)
  int min = 0
  if (claim.hasParam('min')) {
    min = Integer.parseInt(claim.param('min'))
  }
  Farm farm = binding.variables.farm
  People people = new People(farm).bootstrap()
  String message = claim.param('message')
  if (!claim.hasParam('reason')) {
    Logger.info(
      this,
      'Received "Notify All" with no given reason [message: %s]',
      message
    )
  } else if (
    ![ 'RFP', 'Project published', 'New student']
      .contains(claim.param('reason'))
  ) {
    Logger.info(
      this,
      'Received "Notify All" with unknown reason [message: %s, reason: %s]',
      message,
      claim.param('reason')
    )
  }
  for (String uid : people.iterate()) {
    if (people.vacation(uid)) {
      continue
    }
    if (claim.hasParam('reason')) {
      Options options = new Options(new Pmo(farm), uid).bootstrap()
      String reason = claim.param('reason')
      if (reason == 'RFP' && !options.notifyRfps()) {
        continue
      }
      if (reason == 'Project published' && !options.notifyPublish()) {
        continue
      }
      if (reason == 'New student' && !options.notifyStudents()) {
        continue
      }
    }
    int reputation = new Awards(farm, uid).bootstrap().total()
    if (reputation < min) {
      continue
    }
    String tail
    if (min == 0) {
      tail = new Par(
        'You received this message because you are not on vacation, as in §38'
      ).say()
    } else {
      tail = new Par(
        'You received this message because your reputation %+d is over %+d',
        'and you are not on vacation, as in §38'
      ).say(reputation, min)
    }
    claim.copy()
      .type('Notify user')
      .token("user;${uid}")
      .param('message', message + '\n\n' + tail)
      .postTo(new ClaimsOf(farm, project))
  }
}
