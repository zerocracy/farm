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
import com.zerocracy.Par
import com.zerocracy.Project
import com.zerocracy.SoftException
import com.zerocracy.farm.Assume
import com.zerocracy.pm.ClaimIn
import com.zerocracy.pm.ClaimOut
import com.zerocracy.pmo.Awards
import com.zerocracy.pmo.Rfps

def exec(Project pmo, XML xml) {
  new Assume(pmo, xml).isPmo()
  new Assume(pmo, xml).type('Purchase RFP')
  ClaimIn claim = new ClaimIn(xml)
  int rid = Integer.parseInt(claim.param('rfp'))
  Rfps rfps = new Rfps(pmo).bootstrap()
  if (!rfps.exists(rid)) {
    throw new SoftException(
      new Par('RFP #%d doesn\'t exist').say(rid)
    )
  }
  String author = claim.author()
  Awards awards = new Awards(pmo, author).bootstrap()
  int reputation = awards.total()
  if (reputation < 2048) {
    throw new SoftException(
      new Par(
        'Your reputation is %d, it is too low;',
        'must be at least 2048, see §40'
      ).say(reputation)
    )
  }
  String job = 'gh:zerocracy/datum#1'
  int points = -256
  String email = rfps.buy(rid, author)
  String reason = new Par(
    'RFP #%d has been purchased: %s'
  ).say(rid, email)
  awards.add(points, job, new Par.ToText(reason).toString())
  claim.reply(
    new Par(
      'Thanks for purchasing RFP #%d',
      'the email of the client is: %s'
    ).say(rid, email)
  ).postTo(pmo)
  new ClaimOut().type('Notify user').token('user;yegor256').param(
    'message', new Par(
      'RFP #%d has be purchased by @%s: %s'
    ).say(rid, author, email)
  ).param('cause', claim.cid()).postTo(pmo)
}
