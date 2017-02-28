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
package com.zerocracy.stk.pmo.profile.skills

import com.jcabi.aspects.Tv
import com.zerocracy.jstk.SoftException
import com.zerocracy.pm.ClaimIn
import com.zerocracy.pmo.People

assume.type('add skills').exact()

final People people = new People(pmo).bootstrap()
final ClaimIn claim = new ClaimIn(xml)
final String login = claim.param("person")
final Collection<String> skills = people.skills(login)
if (skills.size() > Tv.FIVE) {
  throw new SoftException(
    String.format(
      "You've got too many skills already: `%s` (max is five).",
      String.join("`, `", skills)
    )
  )
}
final String skill = claim.param("skill")
people.skill(login, skill)
claim.reply(
  String.format(
    "New skill \"%s\" added to \"%s\".",
    skill,
    login
  )
).postTo(pmo)
