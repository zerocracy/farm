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
package com.zerocracy.stk.pm.cost

import com.jcabi.github.Coordinates
import com.jcabi.github.Github
import com.jcabi.github.Repo
import com.jcabi.xml.XML
import com.zerocracy.entry.ExtGithub
import com.zerocracy.farm.Assume
import com.zerocracy.jstk.Farm
import com.zerocracy.jstk.Project
import com.zerocracy.jstk.cash.Cash
import com.zerocracy.pm.ClaimOut
import com.zerocracy.pmo.Catalog

def exec(Project project, XML xml) {
  new Assume(project, xml).notPmo()
  new Assume(project, xml).type('Project link was added', 'Project link was removed')
  Farm farm = binding.variables.farm
  Catalog catalog = new Catalog(farm).bootstrap()
  Github github = new ExtGithub(farm).value()
  boolean free = true
  for (String pair : catalog.links(project.pid())) {
    String[] parts = pair.split(':', 2)
    if (parts[0] == 'github') {
      free = !new Repo.Smart(github.repos().get(new Coordinates.Simple(parts[1]))).private
      if (!free) {
        break
      }
    }
  }
  if (free && catalog.fee(project.pid()) != Cash.ZERO) {
    catalog.fee(project.pid(), Cash.ZERO)
    new ClaimOut()
      .type('Notify project')
      .param(
        'message',
        "You don't have any private GitHub repositories any more, the management fee is waived."
      )
      .postTo(project)
  }
  if (!free && catalog.fee(project.pid()) == Cash.ZERO) {
    Cash fee = new Cash.S('$4')
    catalog.fee(project.pid(), fee)
    new ClaimOut()
      .type('Notify project')
      .param(
        'message',
        "Since now you have a private GitHub repository, the management fee ${fee} is applied."
      )
      .postTo(project)
  }
}
