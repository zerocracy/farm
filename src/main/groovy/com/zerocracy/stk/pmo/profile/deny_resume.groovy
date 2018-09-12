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
import com.zerocracy.pmo.Resumes
import org.cactoos.text.FormattedText

def exec(Project pmo, XML xml) {
  new Assume(pmo, xml).isPmo()
  new Assume(pmo, xml).type('Deny resume')
  ClaimIn claim = new ClaimIn(xml)
  String author = claim.author()
  Farm farm = binding.variables.farm
  String login = claim.param('login')
  Resumes resumes = new Resumes(farm).bootstrap()
  if (resumes.examiner(login) != author) {
    throw new SoftException(
      new Par('You are not the examiner of %s, see §1').say(login)
    )
  }
  User.Smart user = new User.Smart(
    new ExtGithub(farm).value().users().get(login)
  )
  if (!user.exists()) {
    resumes.remove(login)
    throw new SoftException(
      new Par(
        'We can\'t find @%s in Github: https://github.com/%1$s: %s'
      ).say(login)
    )
  }
  if (user.type() != 'User') {
    resumes.remove(login)
    throw new SoftException(
      new Par(
        'The GitHub user @%s is not a regular user, but "%s"'
      ).say(login, user.type())
    )
  }

  resumes.remove(login)
  claim.copy()
    .type('Notify user')
    .token("user;${login}")
    .param(
      'message',
      new Par(
        'Your application to Zerocracy has been denied.',
        'You can try again in %d days (see §1).'
      ).say(new Policy().get('1.lag', 16))
    )
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
}
