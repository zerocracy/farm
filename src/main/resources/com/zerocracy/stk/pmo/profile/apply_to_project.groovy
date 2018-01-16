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

import com.jcabi.xml.XML
import com.zerocracy.Par
import com.zerocracy.farm.Assume
import com.zerocracy.Farm
import com.zerocracy.Project
import com.zerocracy.SoftException
import com.zerocracy.cash.Cash
import com.zerocracy.pm.ClaimIn
import com.zerocracy.pm.ClaimOut
import com.zerocracy.pmo.Awards
import com.zerocracy.pmo.Catalog
import com.zerocracy.pmo.People

def exec(Project pmo, XML xml) {
  new Assume(pmo, xml).isPmo()
  new Assume(pmo, xml).type('Apply to a project')
  ClaimIn claim = new ClaimIn(xml)
  Farm farm = binding.variables.farm
  String pid = claim.param('project')
  Catalog catalog = new Catalog(pmo).bootstrap()
  Cash rate = new Cash.S(claim.param('rate'))
  if (!catalog.exists(pid)) {
    throw new SoftException(
      new Par('Project %s doesn\'t exist').say(pid)
    )
  }
  String author = claim.author()
  Cash std = new People(pmo).rate(author)
  if (rate > std) {
    throw new SoftException(
      new Par(
        'Your profile rate is %s,',
        'you can\'t suggest higher rate of %s for this project'
      ).say(std, rate)
    )
  }
  if (rate > new Cash.S('$16') && catalog.sandbox(pid)) {
    throw new SoftException(
      new Par(
        'The rate %s is too high for a sandbox project, sorry, see §33'
      ).say(rate)
    )
  }
  int reputation = new Awards(pmo, author).bootstrap().total()
  if (reputation < 256 && !catalog.sandbox(pid)) {
    throw new SoftException(
      new Par(
        'Your reputation is %d, which is not big enough to apply;',
        'you can only apply to one of our sandbox projects, see §33'
      ).say(reputation)
    )
  }
  if (reputation > 1024 && catalog.sandbox(pid)) {
    throw new SoftException(
      new Par(
        'Your reputation is %d,',
        'which is too high for a sandbox project, see §33'
      ).say(reputation)
    )
  }
  new ClaimOut()
    .type('Notify project')
    .param(
      'message',
      new Par(
        '@%s wants to join you guys.',
        'If you want to add them to the project,',
        'just assign `DEV` role and that\'s it.',
        'The hourly rate suggested is %s (profile rate is %s).',
        'You can use that rate or define another one, see §13.'
      ).say(claim.author(), rate, std)
    )
    .postTo(farm.find("@id='${pid}'")[0])
  claim.reply(
    new Par(
      'The project %s was notified about your desire to join them'
    ).say(pid)
  ).postTo(pmo)
}
