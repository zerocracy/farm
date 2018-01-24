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
package com.zerocracy.stk.pm

import com.jcabi.xml.XML
import com.zerocracy.Farm
import com.zerocracy.Par
import com.zerocracy.Project
import com.zerocracy.farm.Assume
import com.zerocracy.farm.props.Props
import com.zerocracy.pm.ClaimIn
import com.zerocracy.pm.ClaimOut
import com.zerocracy.pmo.Catalog
import twitter4j.Twitter
import twitter4j.TwitterFactory
import twitter4j.auth.AccessToken

def exec(Project project, XML xml) {
  new Assume(project, xml).notPmo()
  new Assume(project, xml).type('Publish the project')
  ClaimIn claim = new ClaimIn(xml)
  String mode = claim.param('mode')
  Farm farm = binding.variables.farm
  Catalog catalog = new Catalog(farm)
  if ('on' == mode) {
    catalog.publish(project.pid(), true)
    claim.reply(
      new Par(
        'The project is visible now at the [board](/board), according to §26'
      ).say()
    ).postTo(project)
    String message = new Par('The project %s was published by @%s')
      .say(project.pid(), claim.author())
    new ClaimOut().type('Notify user').token('user;yegor256')
      .param('message', message).postTo(project)
    Props props = new Props(farm)
    // @todo #404:30min Use of the Twitter API in this project is neither reusable
    //  nor testable. Let's introduce an interface that will serve as our frontend
    //  for the Twitter API. We should have a real implementation and also a fake
    //  one for testing. This interface should be used where the twitter API is
    //  currently used, and we should add tests for the usage of Twitter.
    Twitter twitter = new TwitterFactory().instance
    twitter.setOAuthConsumer(props.get('//twitter/key'), props.get('//twitter/secret'))
    twitter.setOAuthAccessToken(
      new AccessToken(props.get('//twitter/token'), props.get('//twitter/tsecret'))
    )
    twitter.updateStatus(message)
  } else if ('off' == mode) {
    catalog.publish(project.pid(), false)
    claim.reply(
      new Par(
        'The project is not visible anymore at the [board](/board), as in §26'
      ).say()
    ).postTo(project)
    new ClaimOut().type('Notify user').token('user;yegor256').param(
      'message', new Par(
        'The project %s was unpublished by @%s'
      ).say(project.pid(), claim.author())
    ).postTo(project)
  } else {
    claim.reply(
      new Par(
        "Incorrect mode, possible values are 'on' or 'off', see §26"
      ).say()
    ).postTo(project)
  }
}
