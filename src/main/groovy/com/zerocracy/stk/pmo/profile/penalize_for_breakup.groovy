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
package com.zerocracy.stk.pmo.profile

import com.jcabi.xml.XML
import com.zerocracy.Farm
import com.zerocracy.Par
import com.zerocracy.Policy
import com.zerocracy.Project
import com.zerocracy.entry.ClaimsOf
import com.zerocracy.farm.Assume
import com.zerocracy.claims.ClaimIn
import com.zerocracy.pm.staff.Roles
import com.zerocracy.pmo.Awards
import com.zerocracy.pmo.Pmo

def exec(Project pmo, XML xml) {
  new Assume(pmo, xml).isPmo()
  new Assume(pmo, xml).type('Breakup')
  ClaimIn claim = new ClaimIn(xml)
  String author = claim.author()
  Farm farm = binding.variables.farm
  if (new Roles(new Pmo(farm)).bootstrap().hasAnyRole(author)) {
    return
  }
  String student = claim.param('login')
  String job = 'gh:zerocracy/datum#1'
  int points = -new Policy().get('47.penalty', 256)
  String reason = new Par(
    'Penalize for breakup with %s'
  ).say(student)
  new Awards(farm, author).bootstrap().add(pmo, points, job, 'Penalize for breakup')
  claim.copy()
    .type('Award points were added')
    .param('job', job)
    .param('login', author)
    .param('points', points)
    .param('reason', reason)
    .postTo(new ClaimsOf(farm))
  claim.reply(
    new Par(
      'Since you broke up with student %s,',
      'we deducted %d points from you in accordance with §47.'
    ).say(student, -points)
  ).postTo(new ClaimsOf(farm))
}
