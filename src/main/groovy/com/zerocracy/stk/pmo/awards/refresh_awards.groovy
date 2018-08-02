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
import com.zerocracy.Policy
import com.zerocracy.Project
import com.zerocracy.entry.ClaimsOf
import com.zerocracy.farm.Assume
import com.zerocracy.claims.ClaimIn
import com.zerocracy.pmo.Awards
import com.zerocracy.pmo.People

def exec(Project pmo, XML xml) {
  new Assume(pmo, xml).isPmo()
  new Assume(pmo, xml).type('Ping daily')
  ClaimIn claim = new ClaimIn(xml)
  Date outdated = claim.created() - new Policy().get('18.days', 90)
  Farm farm = binding.variables.farm
  new People(farm).bootstrap().iterate().each {
    Awards awards = new Awards(farm, it).bootstrap()
    int before = awards.total()
    awards.removeOlderThan(outdated)
    int after = awards.total()
    // @todo #1218:30min refresh_awards.groovy is submitting claims for all
    //  users in Zerocracy even if reputation didn't change. Fix
    //  refresh_awards.groovy so it only submit claims when there is a
    //  reputation change for user and uncomment tests in
    //  dont_refresh_awards_when_no_change/_after.groovy
    claim.copy()
      .type('Award points were added')
      .param('login', it)
      .param('points', after - before)
      .param('reason', 'fresh awards')
      .postTo(new ClaimsOf(farm))
  }
}
