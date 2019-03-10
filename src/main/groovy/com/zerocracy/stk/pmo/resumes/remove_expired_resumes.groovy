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
package com.zerocracy.stk.pmo.resumes

import com.jcabi.xml.XML
import com.zerocracy.Farm
import com.zerocracy.Par
import com.zerocracy.Policy
import com.zerocracy.Project
import com.zerocracy.claims.ClaimIn
import com.zerocracy.entry.ClaimsOf
import com.zerocracy.farm.Assume
import com.zerocracy.pmo.Resumes
import java.time.Duration
import java.time.Instant

def exec(Project pmo, XML xml) {
  new Assume(pmo, xml).isPmo().type('Ping daily')
  ClaimIn claim = new ClaimIn(xml)
  Farm farm = binding.variables.farm
  Instant expiration = claim.created().toInstant() -
    Duration.ofDays(new Policy(farm).get('1.lag', 16))
  Resumes resumes = new Resumes(farm).bootstrap()
  int penalty = new Policy(farm).get('1.penalty', -32)
  resumes.olderThan(expiration).each { resume ->
    String examiner = resumes.examiner(resume)
    resumes.remove(resume)
    claim.copy()
      .type('Notify user')
      .token("user;${resume}")
      .param(
      'message',
      'Your resume was expired, you can try again to submit join form'
      ).postTo(new ClaimsOf(farm, pmo))
    claim.copy()
      .type('Add award points')
      .param('job', 'none')
      .param('login', examiner)
      .param(
      'reason',
        new Par('%s resume assigned to you was expired, see §1').say(resume)
      )
      .param('minutes', penalty)
      .postTo(new ClaimsOf(farm, pmo))
  }
}
