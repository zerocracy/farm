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
package com.zerocracy.bundles.notify_in_slack_without_user

import com.jcabi.xml.XML
import com.ullink.slack.simpleslackapi.SlackChannel
import com.ullink.slack.simpleslackapi.SlackSession
import com.ullink.slack.simpleslackapi.SlackUser
import com.zerocracy.Farm
import com.zerocracy.Project
import com.zerocracy.entry.ExtSlack
import com.zerocracy.pm.ClaimOut
import org.cactoos.list.SolidList
import org.mockito.Mockito

/**
 * @todo #89:30min Add a new class FkSkSession that implements interface
 *  SkSession, and then use this new class in tests instead of using Mockito.
 *  This task can be implemented only when SkSession replaces SlackSession.
 */
def exec(Project project, XML xml) {
  String channelId = 'C123'
  binding.variables.slack_testing = true
  SlackSession session = Mockito.mock(SlackSession)
  Mockito.when(session.findUserByUserName(Mockito.any(String)))
    .thenReturn(null)
  Mockito.when(session.openDirectMessageChannel(Mockito.<SlackUser>isNull()))
    .thenThrow(NullPointerException)
  SlackChannel channel = Mockito.mock(SlackChannel)
  Mockito.when(channel.id).thenReturn(channelId)
  Mockito.when(session.channels).thenReturn(new SolidList<>(channel))
  Farm farm = binding.variables.farm
  new ExtSlack(farm).value()[channelId] = session
  new ClaimOut()
    .type('Notify in Slack')
    .token("slack;${channelId};none;one-more-part")
    .param('message', 'Hello None!')
    .postTo(project)
}


