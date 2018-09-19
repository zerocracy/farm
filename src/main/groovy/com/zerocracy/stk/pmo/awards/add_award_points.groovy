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
package com.zerocracy.stk.pmo.awards

import com.jcabi.xml.XML
import com.zerocracy.Farm
import com.zerocracy.Par
import com.zerocracy.Project
import com.zerocracy.entry.ClaimsOf
import com.zerocracy.farm.Assume
import com.zerocracy.claims.ClaimIn
import com.zerocracy.pmo.Awards

def exec(Project project, XML xml) {
  new Assume(project, xml).type('Make payment', 'Add award points')
  ClaimIn claim = new ClaimIn(xml)
  if (!claim.hasParam('minutes')) {
    return
  }
  String job = claim.param('job')
  String login = claim.param('login')
  int minutes = Integer.parseInt(claim.param('minutes'))
  if (minutes == 0) {
    return
  }
  String reason = claim.param('reason')
  Farm farm = binding.variables.farm
  Awards awards = new Awards(farm, login).bootstrap()
  awards.add(project, minutes, job, new Par.ToText(reason).toString())
  claim.copy()
    .type('Award points were added')
    .param('job', job)
    .param('login', login)
    .param('points', minutes)
    .param('reason', reason)
    .postTo(new ClaimsOf(farm, project))
}
