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
import com.zerocracy.Policy
import com.zerocracy.Project
import com.zerocracy.SoftException
import com.zerocracy.cash.Cash
import com.zerocracy.entry.ClaimsOf
import com.zerocracy.farm.Assume
import com.zerocracy.claims.ClaimIn
import com.zerocracy.pm.staff.Roles
import com.zerocracy.pmo.Awards
import com.zerocracy.pmo.Catalog
import com.zerocracy.pmo.People

def exec(Project pmo, XML xml) {
  new Assume(pmo, xml).isPmo()
  new Assume(pmo, xml).type('Apply to a project')
  ClaimIn claim = new ClaimIn(xml)
  Farm farm = binding.variables.farm
  String pid = claim.param('pid')
  Catalog catalog = new Catalog(farm).bootstrap()
  Cash rate = new Cash.S(claim.param('rate'))
  if (!catalog.exists(pid)) {
    throw new SoftException(
      new Par('Project %s doesn\'t exist').say(pid)
    )
  }
  String author = claim.author()
  People people = new People(farm).bootstrap()
  Cash std = people.rate(author)
  if (rate > std) {
    throw new SoftException(
      new Par(
        farm,
        'Your profile rate is %s,',
        'you can\'t suggest higher rate of %s for the project %s;',
        'the rate you want the project to pay you must be lower or equal',
        'to your profile rate'
      ).say(std, rate, pid)
    )
  }
  if (rate > new Policy().get('33.max-sandbox-rate', Cash.ZERO)
    && catalog.sandbox().contains(pid)) {
    throw new SoftException(
      new Par(
        farm,
        'The rate %s is too high for a sandbox project %s, sorry, see §33'
      ).say(rate, pid)
    )
  }
  if (rate > Cash.ZERO && people.details(author).empty) {
    throw new SoftException(
      new Par(
        'In order to work for money you have to identify yourself first;',
        'please, click this link and follow the instructions:',
        'https://www.0crat.com/identify'
      ).say()
    )
  }
  Roles roles = new Roles(farm.find("@id='${pid}'")[0]).bootstrap()
  if (!roles.hasAnyRole(author)) {
    int reputation = new Awards(farm, author).bootstrap().total()
    if (reputation < new Policy().get('33.min-live', 256)
      && !catalog.sandbox().contains(pid)) {
      throw new SoftException(
        new Par(
          farm,
          'Your reputation is %d, which is not big enough to apply to %s;',
          'you can only apply to one of our sandbox projects, see §33'
        ).say(reputation, pid)
      )
    }
    if (reputation > new Policy().get('33.max-sandbox-rep', 256)
      && catalog.sandbox().contains(pid)) {
      throw new SoftException(
        new Par(
          farm,
          'Your reputation is %d,',
          'which is too high for a sandbox project %s, see §33'
        ).say(reputation, pid)
      )
    }
  }
  Project project = farm.find("@id='${pid}'")[0]
  claim.copy()
    .type('Notify project')
    .param(
      'message',
      new Par(
        farm,
        '@%s wants to join you guys;',
        'if you want to add them to the project %s,',
        'just assign `DEV` role and that\'s it;',
        'the hourly rate suggested is %s (profile rate is %s);',
        'you can use that rate or define another one, see §13'
      ).say(claim.author(), pid, rate, std)
    )
    .postTo(new ClaimsOf(farm, project))
  claim.reply(
    new Par(
      farm,
      'The project %s was notified about your desire to join them'
    ).say(pid)
  ).postTo(new ClaimsOf(farm))
}
