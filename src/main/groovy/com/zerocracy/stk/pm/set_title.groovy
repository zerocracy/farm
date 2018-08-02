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
import com.zerocracy.Par
import com.zerocracy.entry.ClaimsOf
import com.zerocracy.farm.Assume
import com.zerocracy.Farm
import com.zerocracy.Project
import com.zerocracy.SoftException
import com.zerocracy.claims.ClaimIn
import com.zerocracy.pmo.Catalog

def exec(Project project, XML xml) {
  new Assume(project, xml).notPmo()
  new Assume(project, xml).type('Set title')
  new Assume(project, xml).roles('PO')
  ClaimIn claim = new ClaimIn(xml)
  String pid = project.pid()
  Farm farm = binding.variables.farm
  Catalog catalog = new Catalog(farm).bootstrap()
  if (!claim.hasParam('title')) {
    throw new SoftException(
      new Par(
        'Project title is "%s".',
        'To change it just say `title MyProject`, for example.'
      ).say(catalog.title(pid))
    )
  }
  String title = claim.param('title')
  catalog.title(pid, title)
  claim.reply(
    new Par(
      'The title changed to "%s"'
    ).say(title)
  ).postTo(new ClaimsOf(farm, project))
}
