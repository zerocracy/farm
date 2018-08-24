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

import com.jcabi.github.User
import com.jcabi.xml.XML
import com.zerocracy.Farm
import com.zerocracy.Par
import com.zerocracy.Policy
import com.zerocracy.Project
import com.zerocracy.SoftException
import com.zerocracy.claims.ClaimIn
import com.zerocracy.entry.ClaimsOf
import com.zerocracy.entry.ExtGithub
import com.zerocracy.farm.Assume
import com.zerocracy.pm.staff.Roles
import com.zerocracy.pmo.Exam
import com.zerocracy.pmo.People
import com.zerocracy.pmo.Pmo
import com.zerocracy.pmo.Resumes
import org.cactoos.Scalar
import org.cactoos.iterable.ItemAt
import org.cactoos.iterable.Mapped
import org.cactoos.list.ListOf
import org.cactoos.scalar.Or
import org.cactoos.scalar.StickyScalar
import org.cactoos.text.FormattedText

def exec(Project pmo, XML xml) {
  new Assume(pmo, xml).isPmo()
  new Assume(pmo, xml).type('Invite a friend')
  ClaimIn claim = new ClaimIn(xml)
  String author = claim.author()
  Farm farm = binding.variables.farm
  new Exam(farm, author).min('1.min-rep', 1024)
  String login = claim.param('login')
  // we allow to invite new students without limitations to PMO users
  // and 'farm' (C3NDPUA8L) QA users
  // see https://github.com/zerocracy/farm/issues/1410
  Scalar<Boolean> force = new StickyScalar<>(
    new Or(
      new ListOf<>(
        new Scalar<Boolean>() {
          @Override
          Boolean value() throws Exception {
            new Roles(new Pmo(farm)).bootstrap().hasAnyRole(author)
          }
        },
        new ItemAt<>(
          false,
          new Mapped<>(
            { Project project -> new Roles(project).bootstrap().hasRole(author, 'QA') },
            farm.find('@id="C3NDPUA8L"')
          )
        )
      )
    )
  )
  if (new Resumes(farm).bootstrap().examiner(login) != author && !force.value()) {
    throw new SoftException(
      new Par('You are not the examiner of %s, see §1').say(login)
    )
  }
  User.Smart user = new User.Smart(
    new ExtGithub(farm).value().users().get(login)
  )
  if (!user.exists()) {
    throw new SoftException(
      new Par(
        'We can\'t find @%s in Github: https://github.com/%1$s: %s'
      ).say(login)
    )
  }
  if (user.type() != 'User') {
    throw new SoftException(
      new Par(
        'The GitHub user @%s is not a regular user, but "%s"'
      ).say(login, user.type())
    )
  }

  People people = new People(farm).bootstrap()
  people.invite(login, author, force.value())
  String name
  if (user.name()) {
    name = "@${login} (%${user.name()})"
  } else {
    name = "@${login}"
  }
  claim.reply(
    new Par(
      'Thanks, %s can now work with us,',
      'and you are the mentor, see §1',
    ).say(name)
  ).postTo(new ClaimsOf(farm))
  claim.copy()
    .type('Notify user')
    .token("user;${login}")
    .param(
      'message',
      new Par(
        'You have been invited to Zerocracy by @%s, as required in §1;',
        'you can now apply to the projects, see §2'
      ).say(author)
    )
    .postTo(new ClaimsOf(farm))
  claim.copy()
    .type('Add award points')
    .param('login', author)
    .param('job', 'none')
    .param('minutes', -new Policy().get('1.price', 128))
    .param('reason', new Par('Invited @%s').say(login))
    .postTo(new ClaimsOf(farm))
  String reason = new Par('@%s resume examination').say(login)
  int bonus = new Policy().get('1.bonus', 32)
  claim.copy()
    .type('Add award points')
    .param('login', author)
    .param('job', 'none')
    .param('minutes', bonus)
    .param('reason', reason)
    .postTo(new ClaimsOf(farm))
  claim.reply(
    new FormattedText('You received bonus %d points for %s', bonus, reason).asString()
  ).postTo(new ClaimsOf(farm))
  claim.copy().type('Notify PMO').param(
    'message', new Par(
      'New user @%s was invited by @%s'
    ).say(login, author)
  ).postTo(new ClaimsOf(farm))
  claim.copy().type('Tweet').param(
    'par', new Par(
      'A new user just joined us;',
      'please, welcome "%s": https://github.com/%1$s;',
      'you can join too, just fill out this form:',
      'https://www.0crat.com/join',
      '#zerocracy #freelance #remotework'
    ).say(login)
  ).postTo(new ClaimsOf(farm))
}
