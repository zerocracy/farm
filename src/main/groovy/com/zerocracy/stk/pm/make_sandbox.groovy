/*
 * Copyright (c) 2016-2019 Zerocracy
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
import com.zerocracy.Project
import com.zerocracy.SoftException
import com.zerocracy.claims.ClaimIn
import com.zerocracy.claims.ClaimOut
import com.zerocracy.entry.ClaimsOf
import com.zerocracy.farm.Assume
import com.zerocracy.pm.staff.Roles
import com.zerocracy.pmo.Catalog
import com.zerocracy.pmo.Pmo

def exec(Project project, XML xml) {
  new Assume(project, xml).type('Add to sandbox')
  ClaimIn claim = new ClaimIn(xml)
  String pid = project.pid()
  Farm farm = binding.variables.farm
  String login = claim.author()
  boolean pmoUser = new Roles(new Pmo(farm)).hasAnyRole(login)
  Catalog catalog = new Catalog(farm)
  if (claim.hasParam('flag')) {
    boolean flag = claim.param('flag') == 'on'
    if (pmoUser) {
      catalog.sandbox(pid, flag)
      if (flag) {
        claim.reply('Project was added to sandbox projects')
          .postTo(new ClaimsOf(farm, project))
      } else {
        claim.reply('Project was removed from sandbox projects')
          .postTo(new ClaimsOf(farm, project))
      }
      new ClaimOut().type('Notify PMO').param(
        'message', new Par(farm, 'Sandbox flag for project %s was changed: %s').say(pid, flag)
      ).postTo(new ClaimsOf(farm))
    } else {
      throw new SoftException('You are not allowed to change sandbox flag. Only PMO users can do that.')
    }
  } else {
    if (pmoUser || new Roles(project).hasRole(login, 'PO', 'ARC')) {
      claim.reply(
        new Par(farm, 'It is %s project')
          .say(catalog.sandbox(pid) ? 'sandbox' : 'not sandbox')
      ).postTo(new ClaimsOf(farm, project))
    } else {
      throw new SoftException('You are not allowed to check sandbox flag. PMO users, project PO or ARC can do that.')
    }
  }
}
