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
package com.zerocracy.stk.internal


import com.jcabi.log.Logger
import com.jcabi.xml.XML
import com.zerocracy.Farm
import com.zerocracy.Project
import com.zerocracy.SoftException
import com.zerocracy.claims.ClaimIn
import com.zerocracy.entry.ClaimsOf
import com.zerocracy.farm.Assume
import com.zerocracy.farm.props.Props
import com.zerocracy.pm.staff.Roles
import org.cactoos.io.InputOf
import org.cactoos.text.TextOf

def exec(Project project, XML xml) {
  new Assume(project, xml).isPmo().type('Debug')
  new Assume(project, xml).type('Ping hourly')
  Farm farm = binding.variables.farm
  ClaimIn claim = new ClaimIn(xml)
  if (new Props(farm).has('//testing')) {
    Logger.info(this, 'skip in testing mode')
    return
  }
  if (!new Roles(project).hasAnyRole(claim.author())) {
    throw new SoftException('Only PMO user can debug.')
  }
  if (!claim.hasParam('args')) {
    throw new SoftException('Args required')
  }
  String[] args = claim.param('args').split(',')
  ProcessBuilder bld = new ProcessBuilder(args)
  Process proc = bld.start()
  String out = new TextOf(new InputOf(proc.inputStream)).asString()
  String err = new TextOf(new InputOf(proc.errorStream)).asString()
  claim.copy()
    .type('DebugResponse')
    .param('out', out)
    .param('err', err)
    .postTo(new ClaimsOf(farm, project))
}
