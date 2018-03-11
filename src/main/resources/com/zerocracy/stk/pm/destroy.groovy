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
import com.zerocracy.Par
import com.zerocracy.entry.ExtBucket
import com.zerocracy.farm.Assume
import com.zerocracy.farm.S3Farm
import com.zerocracy.Farm
import com.zerocracy.Project
import com.zerocracy.pm.ClaimIn
import com.zerocracy.pm.staff.Roles
import com.zerocracy.pmo.Agenda
import com.zerocracy.pmo.Catalog
import com.zerocracy.pmo.Projects

def exec(Project project, XML xml) {
  new Assume(project, xml).notPmo()
  new Assume(project, xml).type('Destroy')
  new Assume(project, xml).roles('PO')
  Farm farm = binding.variables.farm
  for (String login : new Roles(project).everybody()) {
    new Projects(farm, login).remove(project.pid())
    new Agenda(project, login).removeAll()
  }
  Catalog catalog = new Catalog(farm).bootstrap()
  String prefix = catalog.findByXPath("@id='${project.pid()}'").iterator().next()
  new S3Farm(new ExtBucket().value()).delete(prefix)
  catalog.delete(project.pid())
  new ClaimIn(xml).reply(
    new Par(
      'All project files were destroyed on our servers.',
      'Now you can safely /kick me out of the channel.'
    ).say()
  ).postTo(project)
}
