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
package com.zerocracy.bundles.add_payment_to_debts_if_has_debt

import com.jcabi.github.Repos
import com.jcabi.xml.XML
import com.zerocracy.Farm
import com.zerocracy.Project
import com.zerocracy.cash.Cash
import com.zerocracy.entry.ExtGithub
import com.zerocracy.pm.cost.Ledger
import com.zerocracy.pmo.Debts

def exec(Project project, XML xml) {
  Farm farm = binding.variables.farm
  new ExtGithub(farm).value().repos().create(new Repos.RepoCreate('test', false))
  new Debts(farm).bootstrap().add('paulodamaso', new Cash.S('$5'), 'Five dollars', 'Too small')
  Ledger ledger = new Ledger(farm, project).bootstrap()
  ledger.add(
    new Ledger.Transaction(
      new Cash.S('$1000'),
      'assets', 'cash',
      'income', 'zerocracy',
      'Donated by unknown'
    )
  )
}
