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
package com.zerocracy.stk.pmo.people

import com.jcabi.xml.XML
import com.zerocracy.Farm
import com.zerocracy.Par
import com.zerocracy.Policy
import com.zerocracy.Project
import com.zerocracy.cash.Cash
import com.zerocracy.claims.ClaimIn
import com.zerocracy.entry.ClaimsOf
import com.zerocracy.farm.Assume
import com.zerocracy.pmo.Awards
import com.zerocracy.pmo.People
import org.cactoos.text.FormattedText

def exec(Project pmo, XML xml) {
  new Assume(pmo, xml).isPmo()
  new Assume(pmo, xml).type('Ping hourly')
  Farm farm = binding.variables.farm
  People people = new People(farm).bootstrap()
  ClaimIn claim = new ClaimIn(xml)
  people.iterate().each { uid ->
    if (!people.hasMentor(uid)) {
      return
    }
    String mentor = people.mentor(uid)
    if (mentor == '0crat') {
      return
    }
    int reputation = new Awards(farm, uid).bootstrap().total()
    int threshold = new Policy().get('43.threshold', 2048)
    if (reputation < threshold) {
      return
    }
    people.graduate(uid)
    claim.copy()
      .type('Notify user')
      .token(new FormattedText('user;%s', uid).asString())
      .param(
        'message',
        new Par(
          'Since your reputation is over %d,',
          'you no longer need a mentor, as explained in §43;',
          'you successfully graduated and won\'t pay the tuition fee anymore;',
          'congratulations!'
        ).say(threshold)
      ).postTo(new ClaimsOf(farm))
    claim.copy().type('Notify PMO').param(
      'message', new Par(
        'The user @%s just graduated with reputation of %d!'
      ).say(uid, reputation)
    ).postTo(new ClaimsOf(farm))
    claim.copy()
      .type('Make payment')
      .param('login', mentor)
      .param('job', 'none')
      .param('cash', new Policy(farm).get('43.bonus', new Cash.S('$32')))
      .param(
        'reason',
        new Par(farm, 'Bonus for student @%s graduation according to §43')
          .say(uid)
      ).postTo(new ClaimsOf(farm))
  }
}
