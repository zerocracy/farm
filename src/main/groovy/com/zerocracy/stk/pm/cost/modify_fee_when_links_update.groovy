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
package com.zerocracy.stk.pm.cost

import com.jcabi.github.Coordinates
import com.jcabi.github.Github
import com.jcabi.github.Repo
import com.jcabi.xml.XML
import com.zerocracy.Farm
import com.zerocracy.Par
import com.zerocracy.Policy
import com.zerocracy.Project
import com.zerocracy.cash.Cash
import com.zerocracy.entry.ClaimsOf
import com.zerocracy.entry.ExtGithub
import com.zerocracy.farm.Assume
import com.zerocracy.claims.ClaimIn
import com.zerocracy.pmo.Catalog

def exec(Project project, XML xml) {
  new Assume(project, xml).notPmo()
  new Assume(project, xml).type('Project link was added', 'Project link was removed')
  ClaimIn claim = new ClaimIn(xml)
  Farm farm = binding.variables.farm
  Catalog catalog = new Catalog(farm).bootstrap()
  Github github = new ExtGithub(farm).value()
  boolean free = true
  for (String pair : catalog.links(project.pid())) {
    String[] parts = pair.split(':', 2)
    if (parts[0] == 'github') {
      Repo.Smart repo = new Repo.Smart(
        github.repos().get(new Coordinates.Simple(parts[1]))
      )
      free = repo.exists() && !repo.private
      if (!free) {
        break
      }
    }
  }
  if (free && catalog.fee(project.pid()) != Cash.ZERO) {
    catalog.fee(project.pid(), Cash.ZERO)
    claim.copy()
      .type('Notify project')
      .param(
        'message',
        new Par(
          'You don\'t have any private GitHub repositories any more,',
          'the management fee is waived, see §23'
        ).say()
      )
      .postTo(new ClaimsOf(farm, project))
  }
  if (!free && catalog.fee(project.pid()) == Cash.ZERO) {
    Cash fee = new Policy().get('23.fee', Cash.ZERO)
    catalog.fee(project.pid(), fee)
    claim.copy()
      .type('Notify project')
      .param(
        'message',
        new Par(
          'Since now you have a private GitHub repository,',
          'the management fee %s is applied, see §23'
        ).say(fee)
      )
      .postTo(new ClaimsOf(farm, project))
  }
}
