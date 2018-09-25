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
package com.zerocracy.stk.pm.cost

import com.jcabi.xml.XML
import com.zerocracy.Farm
import com.zerocracy.Par
import com.zerocracy.Project
import com.zerocracy.cash.Cash
import com.zerocracy.entry.ClaimsOf
import com.zerocracy.farm.Assume
import com.zerocracy.claims.ClaimIn
import com.zerocracy.pm.cost.Estimates
import com.zerocracy.pm.cost.Ledger

def exec(Project project, XML xml) {
  new Assume(project, xml).notPmo()
  new Assume(project, xml).type('Ping')
  ClaimIn claim = new ClaimIn(xml)
  Ledger ledger = new Ledger(farm, project).bootstrap()
  Cash cash = ledger.cash()
  Cash locked = new Estimates(farm, project).bootstrap().total()
  Farm farm = binding.variables.farm
  if (ledger.deficit() && cash > locked) {
    ledger.deficit(false)
    claim.copy()
      .type('Notify project')
      .param(
        'message',
        new Par(
          farm,
          'The project %s is properly funded,',
          'cash balance is [%s](/p/%1$s?a=pm/cost/ledger) and',
          '[%s](/p/%1$s?a=pm/cost/estimates) is in active orders;',
          'we will continue to assign jobs to performers.',
        ).say(project.pid(), cash, locked)
      )
      .postTo(new ClaimsOf(farm, project))
  }
  if (!ledger.deficit() && cash < locked) {
    ledger.deficit(true)
    claim.copy()
      .type('Notify project')
      .param(
        'message',
        new Par(
          farm,
          'The project %s is out of funds,',
          'cash balance is [%s](/p/%1$s?a=pm/cost/ledger) and',
          '[%s](/p/%1$s?a=pm/cost/estimates) is in active orders;',
          'we temporarily stop assigning jobs, see §21;',
          'please, [fund the project](/p/%1$s) ASAP'
        ).say(project.pid(), cash, locked)
      )
      .postTo(new ClaimsOf(farm, project))
  }
}
