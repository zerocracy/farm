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
package com.zerocracy.stk.pmo.people

import com.jcabi.github.Github
import com.jcabi.github.User
import com.jcabi.xml.XML
import com.zerocracy.Farm
import com.zerocracy.Par
import com.zerocracy.Project
import com.zerocracy.entry.ExtGithub
import com.zerocracy.farm.Assume
import com.zerocracy.pm.ClaimIn
import com.zerocracy.pmo.People

import java.util.concurrent.TimeUnit

def exec(Project pmo, XML xml) {
  new Assume(pmo, xml).isPmo()
  new Assume(pmo, xml).type('Ping daily')
  People people = new People(pmo).bootstrap()
  ClaimIn claim = new ClaimIn(xml)
  people.iterate().each {
    String username = people.link(it, 'github')
    if (!exists(username)) {
      claim.copy()
        .type('Notify user')
        .token("user;$it")
        .param(
          'message',
          new Par(
            'We can\'t find your Github account: %s, ',
            'please provide correct account and update your links,',
            'otherwise your profile will be deleted in %d hours.'
          ).say(it, 12)
      ).postTo(pmo)
      // @todo #555:30min Let's implement 'Delete user' stakeholder
      //  which should find user by id from 'login' param, check his/her
      //  existence on Github by link and delete if not exist. Also
      //  'remove_stale_users._after' test should be fixed.
      claim.copy()
        .type('Delete user')
        .until(TimeUnit.HOURS.toSeconds(12))
        .param('login', it)
        .postTo(pmo)
    }
  }
}

@SuppressWarnings('CatchException')
boolean exists(String username) {
  Farm farm = binding.variables.farm
  Github github = new ExtGithub(farm).value()
  // @todo #555:30min It's not possible to check user existence because of
  //  jcabi-github bug: https://github.com/jcabi/jcabi-github/issues/1359
  //  let's replace this ugly construction with something else after bug fix.
  try {
    new User.Smart(github.users().get(username)).id()
    return true
  } catch (Exception ignore) {
    return false
  }
}