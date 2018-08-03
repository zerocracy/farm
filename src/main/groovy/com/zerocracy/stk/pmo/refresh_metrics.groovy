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
package com.zerocracy.stk.pmo

import com.jcabi.xml.XML
import com.zerocracy.Farm
import com.zerocracy.Policy
import com.zerocracy.Project
import com.zerocracy.entry.ClaimsOf
import com.zerocracy.farm.Assume
import com.zerocracy.claims.ClaimIn
import com.zerocracy.pmo.People
import com.zerocracy.pmo.Speed
import java.time.Instant

def exec(Project pmo, XML xml) {
  new Assume(pmo, xml).type('Ping daily')
  ClaimIn claim = new ClaimIn(xml)
  Instant outdated = (claim.created() - new Policy().get('18.days', 90)).toInstant()
  Farm farm = binding.variables.farm
  new People(farm).bootstrap().iterate().each {
    login ->
      Speed speed = new Speed(farm, login).bootstrap()
      double before = speed.avg()
      speed.removeOlderThan(outdated)
      double after = speed.avg()
      if (Math.abs(after - before) > 0.001) {
        claim.copy()
          .param('login', login)
          .type('Speed was updated')
          .postTo(new ClaimsOf(farm))
      }
  }
}
