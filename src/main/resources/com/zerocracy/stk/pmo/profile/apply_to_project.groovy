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
import com.zerocracy.farm.Assume
import com.zerocracy.jstk.Farm
import com.zerocracy.jstk.Project
import com.zerocracy.jstk.SoftException
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
  if (!new Catalog(project).bootstrap().exists(pid)) {
    throw new SoftException(
      "Project \"${pid}\" doesn't exist."
    )
  }
  String author = claim.author()
  new ClaimOut()
    .type('Notify project')
    .param(
      'message',
      "@${claim.author()} wants to join you guys:" +
      " [profile](http://www.0crat.com/u/${author})." +
      " If you want to add @${author} to the project, just" +
      ' assign them `DEV` role and that\'s it.' +
      " The hourly rate of @${author} is ${new People(project).rate(author)}." +
      ' You can use that rate or define another one,' +
      ' see [par.13](http://datum.zerocracy.com/pages/policy.html#13).'
    )
    .postTo(farm.find("@id='${pid}'")[0])
  claim.reply(
    "Project `${pid}` was notified about your desire to join them."
  ).postTo(project)
}
