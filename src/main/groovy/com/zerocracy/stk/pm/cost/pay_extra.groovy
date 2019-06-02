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
package com.zerocracy.stk.pm.cost

import com.jcabi.xml.XML
import com.zerocracy.*
import com.zerocracy.cash.Cash
import com.zerocracy.claims.ClaimIn
import com.zerocracy.claims.ClaimOut
import com.zerocracy.entry.ClaimsOf
import com.zerocracy.farm.Assume
import com.zerocracy.pm.cost.Estimates
import com.zerocracy.pm.cost.Ledger
import com.zerocracy.pm.cost.Rates
import com.zerocracy.pm.staff.Roles

import java.time.Duration

def exec(Project project, XML xml) {
  new Assume(project, xml).notPmo()
  new Assume(project, xml).type('Pay extra')
  new Assume(project, xml).roles('ARC', 'PO')
  ClaimIn claim = new ClaimIn(xml)
  String job = claim.param('job')
  String login = claim.param('login')
  int minutes = Integer.parseInt(claim.param('minutes'))
  if (minutes < 0) {
    throw new SoftException(
      new Par('Minutes %d can\'t be negative, see §49').say(minutes)
    )
  }
  int max = new Policy().get('49.max', 120)
  if (minutes > max) {
    throw new SoftException(
      new Par('Minutes %d can\'t be more than %d, see §49').say(minutes, max)
    )
  }
  Farm farm = binding.variables.farm
  Rates rates = new Rates(project).bootstrap()
  if (!rates.exists(login)) {
    throw new SoftException(
      new Par(
        'The user @%s works for free in this project, see §49'
      ).say(login)
    )
  }
  int attempt = Integer.parseInt(claim.param('attempt', '0'))
  if (attempt > 7) {
    throw new SoftException(
      new Par(
        'We failed to pay in %d attempts'
      ).say(attempt)
    )
  }
  if (!canPay(farm, project, rates.rate(login).mul(minutes) / 60)) {
    new ClaimOut()
      .type('Recharge project')
      .param('triggered_by', new ClaimIn(xml).cid())
      .param('force', true)
      .unique('recharge')
      .postTo(new ClaimsOf(farm, project))
    claim.copy()
      .param('attempt', attempt + 1)
      .author(claim.author())
      .token(claim.token())
      .until(Duration.ofMinutes(5))
      .postTo(new ClaimsOf(farm, project))
    throw new SoftException(
      new Par(
        'The project is under-funded, you can\'t do it now, see §49',
        'We just triggered recharge and will retry to make payment in 5 minutes (%d/8).'
      ).say(attempt)
    )
  }
  if (!new Roles(project).bootstrap().hasAnyRole(login)) {
    throw new SoftException(
      new Par(
        'The user @%s is not a member of this project, see §49'
      ).say(login)
    )
  }
  if (new Ledger(farm, project).bootstrap().deficit()) {
    throw new SoftException(
      new Par(
        'The project is underfunded, see §21'
      ).say()
    )
  }
  String author = claim.author()
  claim.copy()
    .type('Make payment')
    .param('login', login)
    .param('minutes', minutes)
    .param('reason', new Par('Direct payment from @%s').say(author))
    .postTo(new ClaimsOf(farm, project))
  claim.copy()
    .type('Add award points')
    .param('login', author)
    .param('minutes', -new Policy().get('49.penalty', 60))
    .param(
      'reason',
      new Par(
        'Direct payment to @%s in %s, which is discouraged, see §49'
      ).say(login, job)
    )
    .postTo(new ClaimsOf(farm, project))
}

static boolean canPay(Farm farm, Project project, Cash price) {
  Estimates estimates = new Estimates(farm, project).bootstrap()
  Ledger ledger = new Ledger(farm, project).bootstrap()
  !ledger.deficit() && ledger.cash() > estimates.total().add(price)
}
