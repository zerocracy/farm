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
import com.zerocracy.Project
import com.zerocracy.SoftException
import com.zerocracy.entry.ClaimsOf
import com.zerocracy.farm.Assume
import com.zerocracy.claims.ClaimIn
import com.zerocracy.pm.in.Orders

def exec(Project pmo, XML xml) {
  new Assume(pmo, xml).isPmo()
  new Assume(pmo, xml).type('Quit a project')
  ClaimIn claim = new ClaimIn(xml)
  Farm farm = binding.variables.farm
  String pid = claim.param('pid')
  Iterable<Project> projects = farm.find("@id='${pid}'")
  if (!projects.iterator().hasNext()) {
    throw new SoftException(
      new Par('The project %s doesn\'t exist.').say(pid)
    )
  }
  Project target = projects[0]
  String author = claim.author()
  Orders orders = new Orders(farm, target).bootstrap()
  for (String job : orders.jobs(author)) {
    claim.copy()
      .type('Cancel order')
      .param('job', job)
      .param('reason', new Par('@%s decided to quit the project').say(author))
      .postTo(new ClaimsOf(farm, target))
  }
  claim.copy()
    .type('Resign all roles')
    .param('login', author)
    .param('reason', 'The user asked to leave')
    .postTo(new ClaimsOf(farm, target))
  claim.reply(
    new Par(
      'You are not in the project %s anymore.'
    ).say(pid)
  ).postTo(new ClaimsOf(farm))
}
