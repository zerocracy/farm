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
package com.zerocracy.stk.pmo

import com.jcabi.xml.XML
import com.zerocracy.Farm
import com.zerocracy.Policy
import com.zerocracy.Project
import com.zerocracy.claims.ClaimIn
import com.zerocracy.entry.ClaimsOf
import com.zerocracy.farm.Assume
import com.zerocracy.pmo.*

import java.time.Instant
import java.time.Period

def exec(Project pmo, XML xml) {
  new Assume(pmo, xml).isPmo().type('Ping daily', 'Refresh metrics force')
  ClaimIn claim = new ClaimIn(xml)
  Farm farm = binding.variables.farm
  Policy policy = new Policy(farm)
  Instant outdated = claim.created().toInstant() - Period.ofDays(policy.get('18.days', 90))
  new People(pmo).bootstrap().iterate().each { login ->
    new Awards(pmo, login).bootstrap().with {
      int before = total()
      removeOlderThan(outdated)
      int after = total()
      if (before != after) {
        claim.copy()
          .type('Award points were added')
          .param('login', login)
          .param('points', after - before)
          .param('reason', 'fresh awards')
          .param('outdated', outdated)
          .postTo(new ClaimsOf(farm))
      }
    }
    new Speed(pmo, login).bootstrap().with {
      double before = avg()
      removeOlderThan(outdated)
      double after = avg()
      if (Math.abs(after - before) > 0.001) {
        claim.copy()
          .type('Speed was updated')
          .param('login', login)
          .param('outdated', outdated)
          .param('before', before)
          .param('after', after)
          .postTo(new ClaimsOf(farm))
      }
    }
    new Blanks(pmo, login).bootstrap().with {
      removeOlderThan(outdated)
      claim.copy()
        .type('Blanks were updated')
        .param('login', login)
        .param('outdated', outdated)
        .postTo(new ClaimsOf(farm))
    }
    new Negligence(pmo, login).bootstrap().with {
      removeOlderThan(outdated)
      claim.copy()
        .type('Negligance was updated')
        .param('login', login)
        .param('outdated', outdated)
        .postTo(new ClaimsOf(farm))
    }
    new Verbosity(pmo, login).bootstrap().with {
      removeOlderThan(outdated)
      claim.copy()
        .type('Verbosity were updated')
        .param('login', login)
        .param('outdated', outdated)
        .postTo(new ClaimsOf(farm))
    }
  }
}
