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
package com.zerocracy.stk.pm.comm

import com.jcabi.log.Logger
import com.jcabi.xml.XML
import com.zerocracy.Farm
import com.zerocracy.Project
import com.zerocracy.claims.ClaimIn
import com.zerocracy.entry.ClaimsOf
import com.zerocracy.entry.ExtSlack
import com.zerocracy.farm.Assume
import com.zerocracy.farm.props.Props
import com.zerocracy.radars.slack.SkSession
import com.zerocracy.radars.slack.SkSession.SkUser

// Token must look like: slack;C43789437;yegor256;direct

def exec(Project project, XML xml) {
  new Assume(project, xml).type('Notify in Slack')
  ClaimIn claim = new ClaimIn(xml)
  String[] parts = claim.token().split(';')
  if (parts[0] != 'slack') {
    throw new IllegalArgumentException(
      "Something is wrong with this token: ${claim.token()}"
    )
  }
  String message = claim.param('message').replaceAll(
    '\\[([^]]+)]\\(([^)]+)\\)', '<$2|$1>'
  )
  Farm farm = binding.variables.farm
  Props props = new Props(farm)
  if (props.has('//testing') && !binding.variables.slack_testing) {
    Logger.info(this, 'Message to Slack [%s]: %s', claim.token(), message)
    return
  }
  String cid = parts[1]
  SkSession session = session(cid)
  if (parts.length > 2) {
    String uid = parts[2]
    SkUser user = session.user(uid)
    if (user == null) {
      claim.copy()
        .type('Error')
        .param('message', "Can't find ${uid} in Slack session for ${cid}")
        .postTo(new ClaimsOf(farm, project))
      return
    }
    if (parts.length > 3) {
      user.send(message)
    } else {
      session.channel(cid).send("<@${uid}> ${message}")
    }
  } else {
    session.channel(cid).send(message)
  }
}

SkSession session(String channel) {
  Farm farm = binding.variables.farm
  for (SkSession session : new ExtSlack(farm).value().values()) {
    if (session.hasChannel(channel)) {
      return session
    }
  }
  throw new IllegalArgumentException(
    String.format(
      'Can\'t find Slack session for channel "%s", among %d session(s)',
      channel, new ExtSlack(farm).value().size()
    )
  )
}
