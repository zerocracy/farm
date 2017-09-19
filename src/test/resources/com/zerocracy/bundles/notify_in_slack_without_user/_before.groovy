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
package com.zerocracy.bundles.notify_in_slack_without_user

import com.jcabi.xml.XML
import com.ullink.slack.simpleslackapi.SlackChannel
import com.ullink.slack.simpleslackapi.SlackSession
import com.ullink.slack.simpleslackapi.SlackUser
import com.zerocracy.jstk.Project
import com.zerocracy.pm.ClaimOut
import org.cactoos.list.ListOf
import org.mockito.Mockito

/**
 * @todo #62:30min We have to implement simple
 *  slack session object to use inside 'notify_in_slack'
 *  stakeholder-script and fake object for tests.
 *  This session object may wrap `SlackSession` from
 *  `com.ullink.slack.simpleslackapi`.
 *  After that we should get rid of `Mockito` in this test.
 */
def exec(Project project, XML xml) {
  final channelId = 'C123'
  binding.variables.properties.put('slack_testing', true)
  final session = Mockito.mock(SlackSession.class)
  Mockito.when(session.findUserByUserName(Mockito.any(String.class)))
    .thenReturn(null)
  Mockito.when(session.openDirectMessageChannel(Mockito.<SlackUser>isNull()))
    .thenThrow(NullPointerException.class)
  final channel = Mockito.mock(SlackChannel.class)
  Mockito.when(channel.id).thenReturn(channelId)
  Mockito.when(session.channels).thenReturn(new ListOf<>(channel))
  binding.variables.slack.put(channelId, session)
  new ClaimOut()
    .type("Notify in Slack")
    .token("slack;${channelId};none;one-more-part")
    .param("message", "Hello None!")
    .postTo(project)
}


