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
package com.zerocracy.stk.pm

import com.jcabi.xml.XML
import com.zerocracy.Farm
import com.zerocracy.Par
import com.zerocracy.Policy
import com.zerocracy.Project
import com.zerocracy.SoftException
import com.zerocracy.entry.ClaimsOf
import com.zerocracy.farm.Assume
import com.zerocracy.claims.ClaimIn
import com.zerocracy.pmo.Catalog
import com.zerocracy.pmo.Exam

def exec(Project project, XML xml) {
  new Assume(project, xml).notPmo()
  new Assume(project, xml).type('Publish the project')
  ClaimIn claim = new ClaimIn(xml)
  String mode = claim.param('mode')
  Farm farm = binding.variables.farm
  Catalog catalog = new Catalog(farm).bootstrap()
  String pid = project.pid()
  if ('on' == mode) {
    if (catalog.published(pid)) {
      throw new SoftException(
        new Par(
          farm,
          'The project %s is already published on the [board](/board)'
        ).say(pid)
      )
    }
    new Exam(farm, claim.author()).min('26.min-rep', 1024)
    catalog.publish(pid, true)
    claim.reply(
      new Par(
        'The project is visible now at the [board](/board), according to §26'
      ).say()
    ).postTo(new ClaimsOf(farm, project))
    claim.copy()
      .type('Project was published')
      .param('pid', pid)
      .postTo(new ClaimsOf(farm, project))
    claim.copy().type('Tweet').param(
      'par', new Par(
        farm,
        'A new project "%s" is looking for developers,',
        'feel free to apply and join: https://www.0crat.com/board'
      ).say(project.pid())
    ).postTo(new ClaimsOf(farm, project))
    claim.copy()
      .type('Make payment')
      .param('login', claim.author())
      .param('job', 'none')
      .param('minutes', -new Policy().get('26.price', 256))
      .param('reason', new Par('Project %s was published on the board').say(project.pid()))
      .postTo(new ClaimsOf(farm, project))
    claim.copy().type('Notify all').param(
      'message',
      new Par(
        farm,
        'The project %s was published by @%s;',
        'feel free to apply, as explained in §2'
      ).say(pid, claim.author())
    ).param('min', new Policy().get('33.min-live', 0)).postTo(new ClaimsOf(farm, project))
  } else if ('off' == mode) {
    if (!catalog.published(pid)) {
      throw new SoftException(
        new Par(
          farm,
          'The project %s is not published on the [board](/board) yet'
        ).say(pid)
      )
    }
    catalog.publish(pid, false)
    claim.reply(
      new Par(
        'The project is not visible anymore at the [board](/board), as in §26'
      ).say()
    ).postTo(new ClaimsOf(farm, project))
    claim.copy().type('Notify PMO').param(
      'message', new Par(
        farm,
        'The project %s was unpublished by @%s'
      ).say(pid, claim.author())
    ).postTo(new ClaimsOf(farm, project))
  } else {
    claim.reply(
      new Par(
        "Incorrect mode, possible values are 'on' or 'off', see §26"
      ).say()
    ).postTo(new ClaimsOf(farm, project))
  }
}
