/**
 * Copyright (c) 2016-2017 Zerocracy
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
package com.zerocracy.stk.pmo.links

import com.jcabi.xml.XML
import com.zerocracy.farm.Assume
import com.zerocracy.jstk.Project
import com.zerocracy.jstk.SoftException
import com.zerocracy.pm.ClaimIn
import com.zerocracy.pm.ClaimOut
import com.zerocracy.pmo.Catalog

def exec(Project project, XML xml) {
  new Assume(project, xml).isPmo()
  new Assume(project, xml).type('Add link')
  new Assume(project, xml).roles('PO')
  ClaimIn claim = new ClaimIn(xml)
  String pid = claim.param('project')
  String rel = claim.param('rel')
  String href = claim.param('href')
  Catalog catalog = new Catalog(project).bootstrap()
  if (catalog.hasLink(pid, rel, href)) {
    throw new SoftException(
      "Project `${pid}` already has link, rel=`${rel}`, href=`${href}`"
    )
  }
  catalog.link(pid, rel, href)
  claim.reply(
    String.format(
      'The project is linked with rel=`%s` and href=`%s`.',
      rel, href
    )
  ).postTo(project)
  new ClaimOut()
    .type('Project link was added')
    .param('project', pid)
    .param('rel', rel)
    .param('href', href)
    .postTo(project)
}
