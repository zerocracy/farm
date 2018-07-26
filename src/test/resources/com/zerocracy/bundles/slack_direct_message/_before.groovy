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
package com.zerocracy.bundles.slack_direct_message

import com.jcabi.xml.XML
import com.ullink.slack.simpleslackapi.SlackChannel
import com.ullink.slack.simpleslackapi.SlackUser
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted
import com.zerocracy.Farm
import com.zerocracy.Project
import com.zerocracy.radars.slack.ReProfile
import com.zerocracy.radars.slack.SkSession
import org.mockito.Mockito

def exec(Project project, XML xml) {
  SkSession session = Mockito.mock(SkSession)
  String person = '7YYZZT99S'
  SlackMessagePosted event = mockSession('@0crat hello', 'C123', 'user', person)
  Farm farm = binding.variables.farm
  new ReProfile().react(farm, event, session)
}

static mockSession(String message, String channelId, String senderName, String senderId) {
  SlackMessagePosted event = Mockito.mock(SlackMessagePosted)
  Mockito.when(event.messageContent).thenReturn(message)
  SlackChannel channel = Mockito.mock(SlackChannel)
  Mockito.when(channel.id).thenReturn(channelId)
  Mockito.when(event.channel).thenReturn(channel)
  SlackUser sender = Mockito.mock(SlackUser)
  Mockito.when(sender.userName).thenReturn(senderName)
  Mockito.when(sender.id).thenReturn(senderId)
  Mockito.when(event.sender).thenReturn(sender)
  event
}
