/**
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
import com.zerocracy.farm.Assume
import com.zerocracy.jstk.Project
import com.zerocracy.jstk.SoftException
import com.zerocracy.pm.ClaimIn
import com.zerocracy.pmo.People

def exec(Project project, XML xml) {
  new Assume(project, xml).isPmo()
  new Assume(project, xml).type('Change vacation mode')
  ClaimIn claim = new ClaimIn(xml)
  String mode = claim.param('mode')
  People people = new People(project).bootstrap()
  if ('on' == mode) {
    people.vacation(claim.author(), true)
    if (claim.hasToken()) {
      claim.reply('You are on vacation now').postTo(project)
    }
  } else if ('off' == mode) {
    people.vacation(claim.author(), false)
    if (claim.hasToken()) {
      claim.reply('Your vacation has been ended').postTo(project)
    }
  } else {
    throw new SoftException(
      "Incorrect vacation mode. Possible modes are 'on' or 'off'"
    )
  }
}
