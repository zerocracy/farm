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
package com.zerocracy.stk.pmo.profile

import com.jcabi.xml.XML
import com.zerocracy.Farm
import com.zerocracy.Par
import com.zerocracy.Project
import com.zerocracy.SoftException
import com.zerocracy.entry.ClaimsOf
import com.zerocracy.farm.Assume
import com.zerocracy.claims.ClaimIn
import com.zerocracy.pmo.People
import org.cactoos.text.FormattedText

def exec(Project project, XML xml) {
  new Assume(project, xml).isPmo()
  new Assume(project, xml).type('Breakup')
  Farm farm = binding.variables.farm
  ClaimIn claim = new ClaimIn(xml)
  String author = claim.author()
  String login = claim.param('login')
  People people = new People(farm).bootstrap()
  if (!people.hasMentor(login)) {
    throw new SoftException(
      new Par('User @%s doesn\'t have a mentor').say(login)
    )
  }
  if (people.mentor(login) != author) {
    throw new SoftException(
      new Par('You are not a mentor of @%s').say(login)
    )
  }
  people.breakup(login)
  claim.reply(
    new Par(
      'User @%s is not your student anymore, see §47'
    ).say(login)
  ).postTo(new ClaimsOf(farm, project))
  claim.copy().type('Notify user')
  .token(
    new FormattedText(
      'user;%s',
      login
    ).asString()
  ).param('message',
    new Par(
      'User @%s is not your mentor anymore, he/she broke up with you, see §47'
    ).say(author)
  ).postTo(new ClaimsOf(farm, project))
}
