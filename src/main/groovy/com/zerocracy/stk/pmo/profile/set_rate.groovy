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
import com.zerocracy.cash.Cash
import com.zerocracy.cash.CashParsingException
import com.zerocracy.entry.ClaimsOf
import com.zerocracy.farm.Assume
import com.zerocracy.claims.ClaimIn
import com.zerocracy.pmo.People

def exec(Project pmo, XML xml) {
  new Assume(pmo, xml).isPmo()
  new Assume(pmo, xml).type('Set rate')
  Farm farm = binding.variables.farm
  People people = new People(farm).bootstrap()
  ClaimIn claim = new ClaimIn(xml)
  String author = claim.author()
  if (!claim.hasParam('rate')) {
    throw new SoftException(
      new Par(
        'Your rate is %s;',
        'to change it just say `rate $25` or `rate 18EUR`, for example;',
        'you can format the rate differently, see §16'
      ).say(people.rate(author))
    )
  }
  Cash rate
  try {
    rate = new Cash.S(claim.param('rate').replaceAll('(.*)([A-Z]{3})$', '$2 $1'))
  } catch (CashParsingException ex) {
    throw new SoftException(
      new Par(
        '%s; use `$25` or `18.50RUR` or `25EUR` or `18GBP`',
      ).say(ex.message)
    )
  }
  if (rate > Cash.ZERO && people.details(author).empty) {
    throw new SoftException(
      new Par(
        'In order to work for money you have to identify yourself first;',
        'please, click this link and follow the instructions:',
        'https://www.0crat.com/identify'
      ).say()
    )
  }
  people.rate(author, rate)
  claim.reply(
    new Par(
      'Rate of @%s set to %s, according to §16'
    ).say(author, rate)
  ).postTo(new ClaimsOf(farm))
}
