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
package com.zerocracy.stk.pmo.awards


import com.jcabi.xml.XML
import com.zerocracy.Farm
import com.zerocracy.Par
import com.zerocracy.Policy
import com.zerocracy.Project
import com.zerocracy.claims.ClaimIn
import com.zerocracy.entry.ClaimsOf
import com.zerocracy.farm.Assume

import java.time.Duration

def exec(Project project, XML xml) {
  new Assume(project, xml).notPmo().type('Order was canceled')
  ClaimIn claim = new ClaimIn(xml)
  String job = claim.param('job')
  Farm farm = binding.variables.farm
  if (claim.hasParam('voluntarily') && claim.param('voluntarily') == 'true') {
    Duration age
    if (claim.hasParam('age')) {
      age = Duration.ofMinutes(Integer.parseInt(claim.param('age')))
    } else {
      claim.reply('penalize_for_refusal: failed to determinate ticket age, using default (3 days)')
        .postTo(new ClaimsOf(farm, project))
      age = Duration.ofDays(2)
    }
    Policy policy = new Policy(farm)
    int penalty
    if (age <= Duration.ofDays(1)) {
      penalty = 0
    } else if (age <= Duration.ofDays(3)) {
      penalty = policy.get('6.penalty-3days', 10)
    } else if (age <= Duration.ofDays(6)) {
      penalty = policy.get('6.penalty-6days', 20)
    } else {
      penalty = policy.get('6.penalty-6days', 50)
    }
    if (penalty == 0) {
      claim.reply(new Par(farm,'Job refused in %d hours - no penalty, see §6').say(age.toHours()))
        .postTo(new ClaimsOf(farm, project))
    } else {
      claim.copy()
        .type('Add award points')
        .param('job', job)
        .param('login', claim.param('login'))
        .param(
        'reason',
          new Par(farm,'Tasks refusal is discouraged (job refused in %d hours), see §6').say(age.toHours())
        )
        .param('minutes', -penalty)
        .postTo(new ClaimsOf(farm, project))
    }
  }
}
