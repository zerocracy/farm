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
package com.zerocracy.stk.pm

import com.jcabi.xml.XML
import com.zerocracy.farm.Assume
import com.zerocracy.jstk.Farm
import com.zerocracy.jstk.Project
import com.zerocracy.jstk.SoftException
import com.zerocracy.pm.ClaimIn
import com.zerocracy.pmo.Catalog
import com.zerocracy.pmo.Pmo

def exec(Project project, XML xml) {
  new Assume(project, xml).type('Set title')
  new Assume(project, xml).roles('PO')
  def claim = new ClaimIn(xml)
  String pid = project.pid()
  Farm farm = binding.variables.farm
  def catalog = new Catalog(new Pmo(farm)).bootstrap()
  if (!claim.hasParam('title')) {
    throw new SoftException(
      "Project title is ${catalog.title(pid)}. " +
      'To change it just say `title MyProject`, for example.'
    )
  }
  def title = claim.param('title')
  catalog.title(pid, title)
  claim.reply("Done, title changed to \"${title}\"").postTo(project)
}
