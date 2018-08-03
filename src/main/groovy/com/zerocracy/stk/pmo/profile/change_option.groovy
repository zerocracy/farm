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
import com.zerocracy.Project
import com.zerocracy.SoftException
import com.zerocracy.entry.ClaimsOf
import com.zerocracy.farm.Assume
import com.zerocracy.claims.ClaimIn
import com.zerocracy.pmo.Options
import com.zerocracy.pmo.Pmo

// @todo #1186:30min Add ability to change options notifyStudents, notifyRfps
//  and notifyPublish. This stakeholder (and maybe Options class itself) most
//  probably will need to be refactored to allow easier addition of any future
//  options. Introduce also handling of `/option` - should display all available
//  options with their current values, and `/option name` - should display
//  current value of a given option.
def exec(Project pmo, XML xml) {
  new Assume(pmo, xml).isPmo()
  new Assume(pmo, xml).type('Change option')
  Farm farm = binding.variables.farm
  ClaimIn claim = new ClaimIn(xml)
  Options options = new Options(new Pmo(farm), claim.author()).bootstrap()
  if (claim.param('name') == 'maxJobsInAgenda') {
    String value = claim.param('value')
    if (!value.integer || value.toInteger() < 1) {
      throw new SoftException(
        new Par(
          'maxJobsInAgenda accepts only positive integers'
        ).say()
      )
    }
    options.maxJobsInAgenda(value.toInteger())
    claim.reply("Your maxJobsInAgenda option is set to ${value.toInteger()}")
      .postTo(new ClaimsOf(farm))
  } else {
    throw new SoftException(
      new Par(
        'Incorrect option;',
        'Possible options are: "maxJobsInAgenda"'
      ).say()
    )
  }
}
