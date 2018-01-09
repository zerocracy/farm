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
import com.zerocracy.jstk.Farm
import com.zerocracy.jstk.Project
import com.zerocracy.jstk.SoftException
import com.zerocracy.jstk.cash.Cash
import com.zerocracy.pm.ClaimIn
import com.zerocracy.pm.ClaimOut
import com.zerocracy.pmo.Catalog
import com.zerocracy.pmo.People

def exec(Project project, XML xml) {
  new Assume(project, xml).isPmo()
  new Assume(project, xml).type('Apply to a project')
  ClaimIn claim = new ClaimIn(xml)
  Farm farm = binding.variables.farm
  String pid = claim.param('project')
  Cash rate = new Cash.S(claim.param('rate'))
  if (!new Catalog(project).bootstrap().exists(pid)) {
    throw new SoftException(
      new Par('Project %s doesn\'t exist').say(pid)
    )
  }
  String author = claim.author()
  Cash std = new People(project).rate(author)
  if (rate > std) {
    throw new SoftException(
      new Par(
        'Your profile rate is %s,',
        'you can\'t suggest higher rate of %s for this project'
      ).say(std, rate)
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
        'The hourly rate suggested by @%1$s is %s (profile rate is %s).',
        'You can use that rate or define another one, see §13.'
      ).say(claim.author(), rate, std)
    )
    .postTo(farm.find("@id='${pid}'")[0])
  claim.reply(
    new Par(
      'The project %s was notified about your desire to join them'
    ).say(pid)
  ).postTo(project)
}
