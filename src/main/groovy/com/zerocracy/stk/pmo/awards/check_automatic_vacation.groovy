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
package com.zerocracy.stk.pmo.awards

import com.jcabi.xml.XML
import com.zerocracy.Farm
import com.zerocracy.Par
import com.zerocracy.Policy
import com.zerocracy.Project
import com.zerocracy.entry.ClaimsOf
import com.zerocracy.farm.Assume
import com.zerocracy.claims.ClaimIn
import com.zerocracy.pmo.Awards
import com.zerocracy.pmo.People
import org.cactoos.collection.Filtered

def exec(Project project, XML xml) {
  new Assume(project, xml).type('Award points were added')
  ClaimIn claim = new ClaimIn(xml)
  String user = claim.param('login')
  Farm farm = binding.variables.farm
  People people = new People(farm).bootstrap()
  if (!people.vacation(user)) {
    Policy policy = new Policy()
    List<Integer> awards = new Awards(farm, user).bootstrap().awards(policy.get('52.days', 16))
    if (
      new Filtered<>({ points -> (points < 0) }, awards).size() >= policy.get('52.awards', 8)
      && new Filtered<>({ points -> (points > 0) }, awards).isEmpty()
    ) {
      people.vacation(user, true)
      claim.copy()
        .type('Notify user')
        .token("user;${user}")
        .param(
        'message',
        new Par('You have too many negative awards and no positive ones, turning vacation on automatically, see §52')
          .say()
      ).postTo(new ClaimsOf(farm, project))
    }
  }
}
