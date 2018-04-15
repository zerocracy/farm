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
package com.zerocracy.stk.pmo.profile

import com.jcabi.github.User
import com.jcabi.xml.XML
import com.zerocracy.Farm
import com.zerocracy.Par
import com.zerocracy.Policy
import com.zerocracy.Project
import com.zerocracy.SoftException
import com.zerocracy.entry.ExtGithub
import com.zerocracy.farm.Assume
import com.zerocracy.pm.ClaimIn
import com.zerocracy.pmo.Exam
import com.zerocracy.pmo.People
import javax.json.JsonObject
import org.cactoos.text.AbbreviatedText

def exec(Project pmo, XML xml) {
  new Assume(pmo, xml).isPmo()
  new Assume(pmo, xml).type('Invite a friend')
  ClaimIn claim = new ClaimIn(xml)
  String author = claim.author()
  Farm farm = binding.variables.farm
  new Exam(farm, author).min('1.min-rep', 1024)
  String login = claim.param('login')
  User.Smart user = new User.Smart(
    new ExtGithub(farm).value().users().get(login)
  )
  JsonObject json
  try {
    json = user.json()
  } catch (AssertionError ex) {
    throw new SoftException(
      new Par(
        'We can\'t find @%s in Github: https://github.com/%1$s: %s'
      ).say(login, new AbbreviatedText(ex.message, 100).asString())
    )
  }
  if (json.getString('type') != 'User') {
    throw new SoftException(
      new Par(
        'The GitHub user @%s is not a regular user, but "%s"'
      ).say(login, json.getString('type'))
    )
  }
  People people = new People(farm).bootstrap()
  people.invite(login, author)
  claim.reply(
    new Par(
      'Thanks, @%s (%s) can now work with us,',
      'and you are the mentor, see §1',
    ).say(login, json.getString('name'))
  ).postTo(pmo)
  claim.copy()
    .type('Notify user')
    .token("user;${login}")
    .param(
      'message',
      new Par(
        'You have been invited to Zerocracy by @%s, as required in §1.',
        'You can now apply to the projects, see §2.'
      ).say(author)
    )
    .postTo(pmo)
  claim.copy()
    .type('Make payment')
    .param('login', author)
    .param('job', 'none')
    .param('minutes', -new Policy().get('1.price', 64))
    .param('reason', new Par('Invited @%s').say(login))
    .postTo(pmo)
  claim.copy().type('Notify PMO').param(
    'message', new Par(
      'New user @%s was invited by @%s'
    ).say(login, author)
  ).postTo(pmo)
}
