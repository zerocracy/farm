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

import com.jcabi.xml.XML
import com.zerocracy.Par
import com.zerocracy.Policy
import com.zerocracy.Project
import com.zerocracy.farm.Assume
import com.zerocracy.pm.ClaimIn
import com.zerocracy.pmo.Awards
import com.zerocracy.pmo.People

def exec(Project pmo, XML xml) {
  new Assume(pmo, xml).isPmo()
  new Assume(pmo, xml).type('Ping hourly')
  People people = new People(pmo).bootstrap()
  ClaimIn claim = new ClaimIn(xml)
  people.iterate().each { uid ->
    if (!people.hasMentor(uid)) {
      return
    }
    if (people.mentor(uid) == '0crat') {
      return
    }
    int reputation = new Awards(pmo, uid).bootstrap().total()
    int threshold = new Policy().get('43.threshold', 2048)
    if (reputation < threshold) {
      return
    }
    people.graduate(uid)
    claim.reply(
      new Par(
        'Since your reputation is over %d,',
        'you don\'t need a mentor anymore, as explained in §43;',
        'you successfully graduated and won\'t pay the tuition fee;',
        'congratulations!'
      ).say(threshold)
    ).postTo(pmo)
    claim.copy().type('Notify PMO').param(
      'message', new Par(
        'The user @%s just graduated with reputation of %d!'
      ).say(uid, reputation)
    ).postTo(pmo)
  }
}
