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
package com.zerocracy.stk.pm.in.links

import com.jcabi.github.Coordinates
import com.jcabi.github.Repo
import com.jcabi.xml.XML
import com.zerocracy.Farm
import com.zerocracy.Par
import com.zerocracy.Project
import com.zerocracy.SoftException
import com.zerocracy.entry.ClaimsOf
import com.zerocracy.entry.ExtGithub
import com.zerocracy.farm.Assume
import com.zerocracy.claims.ClaimIn
import com.zerocracy.pmo.Catalog

def exec(Project project, XML xml) {
  new Assume(project, xml).notPmo()
  new Assume(project, xml).type('Add link')
  new Assume(project, xml).roles('PO', 'ARC')
  ClaimIn claim = new ClaimIn(xml)
  String pid = project.pid()
  String rel = claim.param('rel')
  String href = claim.param('href')
  Farm farm = binding.variables.farm
  if (rel == 'github') {
    Repo.Smart repo = new Repo.Smart(
      new ExtGithub(farm).value().repos().get(new Coordinates.Simple(href))
    )
    if (!repo.exists()) {
      throw new SoftException(
        new Par(
          'I cannot find the repository is either absent or',
          'Zerocrat doesn\'t have proper access: https://github.com/%s'
        ).say(href)
      )
    }
    claim.copy().type('Tweet').param(
      'par', new Par(
        'We started to work with https://github.com/%s',
      ).say(href)
    ).postTo(new ClaimsOf(farm, project))
  }
  Catalog catalog = new Catalog(farm).bootstrap()
  catalog.link(pid, rel, href)
  claim.reply(
    new Par(
      'The project is linked with rel=`%s` and href=`%s`, by §17'
    ).say(rel, href)
  ).postTo(new ClaimsOf(farm, project))
  claim.copy()
    .type('Project link was added')
    .param('rel', rel)
    .param('href', href)
    .postTo(new ClaimsOf(farm, project))
}
