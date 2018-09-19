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
package com.zerocracy.stk.pm.cost.funding

import com.jcabi.xml.XML
import com.zerocracy.Farm
import com.zerocracy.Project
import com.zerocracy.claims.ClaimIn
import com.zerocracy.claims.ClaimOut
import com.zerocracy.entry.ClaimsOf
import com.zerocracy.farm.Assume
import com.zerocracy.farm.props.Props
import com.zerocracy.pmo.recharge.Recharge
import java.time.Duration

/**
 * This stakeholder automatically requests for a recharge the project if it
 * detects a deficit.
 *
 * @param project Project to recharge
 * @param xml Claim
 */
def exec(Project project, XML xml) {
  new Assume(project, xml).notPmo()
  new Assume(project, xml).type('Make payment', 'Ping hourly')
  Farm farm = binding.variables.farm
  if (new Recharge(farm, project).required()) {
    ClaimOut recharge = new ClaimIn(xml).copy()
      .type('Recharge project')
      .unique('recharge')
    if (!new Props(farm).has('//testing')) {
      recharge.until(Duration.ofMinutes(5))
    }
    recharge.postTo(new ClaimsOf(farm, project))
  }
}
