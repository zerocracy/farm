/**
 * Copyright (c) 2016-2017 Zerocracy
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
package com.zerocracy.stk.pmo.profile.wallet

import com.jcabi.xml.XML
import com.zerocracy.farm.Assume
import com.zerocracy.jstk.Farm
import com.zerocracy.jstk.Project
import com.zerocracy.pm.ClaimIn
import com.zerocracy.pmo.People
import com.zerocracy.pmo.Pmo

def exec(Project project, XML xml) {
  new Assume(project, xml).type('Show wallet')
  ClaimIn claim = new ClaimIn(xml)
  Farm farm = binding.variables.farm
  Project pmo = new Pmo(farm)
  claim.reply(response(new People(pmo).bootstrap(), claim.author()))
    .postTo(project)
}

def response(People people, String author) {
  String wallet = people.wallet(author)
  String bank = people.bank(author)
  if (wallet.empty || bank.empty) {
    'Your wallet is not configured yet'
  } else {
    String.format(
      'Your wallet is `%s` at "%s".',
      people.wallet(author),
      people.bank(author)
    )
  }
}
