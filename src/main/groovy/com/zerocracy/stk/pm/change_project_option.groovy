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
package com.zerocracy.stk.pm

import com.jcabi.xml.XML
import com.zerocracy.Farm
import com.zerocracy.Par
import com.zerocracy.Project
import com.zerocracy.SoftException
import com.zerocracy.claims.ClaimIn
import com.zerocracy.entry.ClaimsOf
import com.zerocracy.farm.Assume
import com.zerocracy.pm.PktOptions

def exec(Project pkt, XML xml) {
  new Assume(pkt, xml)
    .notPmo()
    .type('Change project option')
    .roles('PO', 'ARC')
  ClaimIn claim = new ClaimIn(xml)
  Farm farm = binding.variables.farm
  PktOptions options = new PktOptions(pkt, farm).bootstrap()
  if (claim.param('name') == 'daysToCloseTask') {
    String value = claim.param('value')
    if (!value.integer || value.toInteger() < 5) {
      throw new SoftException(
        new Par(
          'daysToCloseTask should be greater than 5'
        ).say()
      )
    }
    options.daysToCloseTask(value.toInteger())
    claim.reply("Project daysToCloseTask option is set to ${value.toInteger()}")
      .postTo(new ClaimsOf(farm))
  } else {
    throw new SoftException(
      new Par(
        'Incorrect option;',
        'Possible options are: "daysToCloseTask"'
      ).say()
    )
  }
}
