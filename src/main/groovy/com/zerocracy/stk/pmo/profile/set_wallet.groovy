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
import com.zerocracy.pmo.People

def exec(Project pmo, XML xml) {
  new Assume(pmo, xml).isPmo()
  new Assume(pmo, xml).type('Set wallet')
  Farm farm = binding.variables.farm
  People people = new People(farm).bootstrap()
  ClaimIn claim = new ClaimIn(xml)
  String author = claim.author()
  if (!claim.hasParam('bank') || !claim.hasParam('wallet')) {
    String wallet = people.wallet(author)
    String bank = people.bank(author)
    if (wallet.empty || bank.empty) {
      throw new SoftException(
        new Par(
          'Your wallet is not configured yet.',
          'To configure it just say `wallet paypal me@example.com`, for example.'
        ).say()
      )
    }
    throw new SoftException(
      new Par(
        'Your wallet is `%s` at "%s"'
      ).say(people.wallet(author), people.bank(author))
    )
  }
  if (people.details(author).empty) {
    throw new SoftException(
      new Par(
        'In order to work for money you have to identify yourself first;',
        'please, click this link and follow the instructions:',
        'https://www.0crat.com/identify'
      ).say()
    )
  }
  String bank = claim.param('bank')
  String wallet = claim.param('wallet')
  people.wallet(author, bank, wallet)
  claim.copy().type('Notify PMO').param(
    'message', new Par(
      'The wallet was modified by @%s, set to `%s` at `%s`'
    ).say(author, wallet, bank)
  ).postTo(new ClaimsOf(farm))
  claim.reply(
    new Par(
      'Wallet of @%s set to `%s:%s`'
    ).say(author, bank, wallet)
  ).postTo(new ClaimsOf(farm))
}
