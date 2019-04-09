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
package com.zerocracy.stk.pmo.profile

import com.jcabi.github.User
import com.jcabi.xml.XML
import com.zerocracy.*
import com.zerocracy.claims.ClaimIn
import com.zerocracy.entry.ClaimsOf
import com.zerocracy.entry.ExtGithub
import com.zerocracy.farm.Assume
import com.zerocracy.pm.staff.GlobalInviters
import com.zerocracy.pmo.Exam
import com.zerocracy.pmo.People
import com.zerocracy.pmo.Resumes
import org.cactoos.text.FormattedText

def exec(Project pmo, XML xml) {
  new Assume(pmo, xml).isPmo()
  new Assume(pmo, xml).type('Invite a friend')
  ClaimIn claim = new ClaimIn(xml)
  String author = claim.author()
  Farm farm = binding.variables.farm
  new Exam(farm, author).min('1.min-rep', 1024)
  String login = claim.param('login')
  Resumes resumes = new Resumes(farm).bootstrap()
  boolean force = new GlobalInviters(farm).contains(author)
  if ((!resumes.exists(login) || resumes.examiner(login) != author) && !force) {
    throw new SoftException(
      new Par(
        'You are not the examiner of %s or resume does not exist, see §1'
      ).say(login)
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
  people.invite(login, author, force)
  resumes.remove(login)
  String name = "@${login}"
  // @todo #1753:30min It's a temporary workaround to use `login` as a name, because jcabi-github
  //  can't check that real name is present because of bug
  //  https://github.com/jcabi/jcabi-github/issues/1495
  //  After fix `hasName` in jcabi-github uncomment the code above and remove
  //  name variable initialization.
//  if (user.hasName()) {
//    name = "@${login} (%${user.name()})"
//  } else {
//    name = "@${login}"
//  }
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
    ).postTo(new ClaimsOf(farm))
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
