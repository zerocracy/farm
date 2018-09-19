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
import com.mongodb.client.model.Filters
import com.zerocracy.Farm
import com.zerocracy.Project
import com.zerocracy.claims.ClaimIn
import com.zerocracy.claims.Footprint
import com.zerocracy.entry.ClaimsOf
import com.zerocracy.farm.Assume
import com.zerocracy.pmo.recharge.Recharge

import java.time.Duration
import java.time.Instant

/**
 * This stakeholder recharges a project using the same Stripe account which was
 * used previously to fund the project.
 *
 * @param project Project to recharge
 * @param xml Claim
 */
def exec(Project project, XML xml) {
  new Assume(project, xml).notPmo()
  new Assume(project, xml).type('Recharge project')
  Farm farm = binding.variables.farm
  boolean recharged = new Footprint(farm, project).withCloseable { Footprint footprint ->
    footprint.collection().countDocuments(
      Filters.and(
        Filters.gt(
          'created',
          Date.from(Instant.now() - Duration.ofMinutes(30L))
        ),
        Filters.eq('type', 'Project was recharged')
      )
    ) > 0L
  }
  if (recharged) {
    return
  }
  ClaimIn claim = new ClaimIn(xml)
  Recharge recharge = new Recharge(farm, project)
  if (recharge.exists() && recharge.required()) {
    recharge.pay(claim.copy()).postTo(new ClaimsOf(farm, project))
    claim.copy()
      .type('Project was recharged')
      .postTo(new ClaimsOf(farm, project))
  }
}
