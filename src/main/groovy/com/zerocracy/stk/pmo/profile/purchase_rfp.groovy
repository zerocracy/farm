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
import com.zerocracy.Policy
import com.zerocracy.Project
import com.zerocracy.SoftException
import com.zerocracy.entry.ClaimsOf
import com.zerocracy.farm.Assume
import com.zerocracy.claims.ClaimIn
import com.zerocracy.pmo.Awards
import com.zerocracy.pmo.Exam
import com.zerocracy.pmo.Rfps

def exec(Project pmo, XML xml) {
  new Assume(pmo, xml).isPmo()
  new Assume(pmo, xml).type('Purchase RFP')
  ClaimIn claim = new ClaimIn(xml)
  int rid = Integer.parseInt(claim.param('id'))
  Farm farm = binding.variables.farm
  Rfps rfps = new Rfps(farm).bootstrap()
  if (!rfps.exists(rid)) {
    throw new SoftException(
      new Par('RFP #%d doesn\'t exist, see [full list](/rfps)').say(rid)
    )
  }
  String author = claim.author()
  new Exam(farm, author).min('40.min', 512)
  String job = 'gh:zerocracy/datum#1'
  int points = new Policy().get('40.price', -256)
  String email = rfps.buy(rid, author)
  String reason = new Par(
    'RFP #%d has been purchased: %s'
  ).say(rid, email)
  Awards awards = new Awards(farm, author)
  awards.add(pmo, points, job, new Par.ToText(reason).toString())
  claim.copy()
    .type('Award points were added')
    .param('job', job)
    .param('login', author)
    .param('points', points)
    .param('reason', reason)
    .postTo(new ClaimsOf(farm))
  claim.reply(
    new Par(
      'Thanks for purchasing RFP #%d;',
      'the email of the client is %s;',
      'we deducted %d points from your reputation, according to §40'
    ).say(rid, email, -points)
  ).postTo(new ClaimsOf(farm))
  claim.copy().type('Notify user').token("user;${owner}").param(
    'message',
    new Par(
      'Your RFP #%d has been purchased by @%s;',
      'he/she will get in touch with you shortly,',
      'since he/she now knows your email;',
      'wish you luck in your new project!'
    ).say(rid, author)
  ).postTo(new ClaimsOf(farm))
  claim.copy().type('Notify PMO').param(
    'message', new Par(
      'RFP #%d has been purchased by @%s: %s'
    ).say(rid, author, email)
  ).postTo(new ClaimsOf(farm))
}
