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
package com.zerocracy.stk.pmo.catalog

import com.jcabi.github.Coordinates
import com.jcabi.github.Github
import com.jcabi.github.Language
import com.jcabi.xml.XML
import com.zerocracy.Farm
import com.zerocracy.Project
import com.zerocracy.Txn
import com.zerocracy.cash.Cash
import com.zerocracy.entry.ExtGithub
import com.zerocracy.farm.Assume
import com.zerocracy.pm.cost.Estimates
import com.zerocracy.pm.cost.Ledger
import com.zerocracy.pm.in.Orders
import com.zerocracy.pm.scope.Wbs
import com.zerocracy.pm.staff.Roles
import com.zerocracy.pmo.Catalog
import com.zerocracy.pmo.Pmo

def exec(Project pkt, XML xml) {
  new Assume(pkt, xml).notPmo().type('Ping daily')
  Farm farm = binding.variables.farm
  Ledger ledger = new Ledger(farm, pkt).bootstrap()
  Wbs wbs = new Wbs(pkt).bootstrap()
  Orders orders = new Orders(farm, pkt).bootstrap()
  Estimates est = new Estimates(farm, pkt).bootstrap()
  Roles roles = new Roles(pkt).bootstrap()
  Github github = new ExtGithub(farm).value()
  new Txn(new Pmo(farm)).withCloseable { pmo ->
    Catalog catalog = new Catalog(pmo).bootstrap()
    catalog.jobs(pkt.pid(), wbs.iterate().size())
    catalog.orders(pkt.pid(), orders.iterate().size())
    Cash cash = ledger.cash().add(est.total().mul(-1L))
    if (cash < Cash.ZERO) {
      cash = Cash.ZERO
    }
    catalog.cash(pkt.pid(), cash, ledger.deficit())
    catalog.members(pkt.pid(), roles.everybody())
    List<String> arcs = roles.findByRole('ARC')
    String arc = '0crat'
    if (!arcs.empty) {
      arc = arcs[0]
    }
    catalog.architect(pkt.pid(), arc)
    Iterable<String> repos = catalog.links(pkt.pid(), 'github')
    catalog.languages(pkt.pid(), languages(github, repos))
    pmo.commit()
  }
}

/**
 * Get languages from repos.
 * @param repos Github repositories
 * @return Languages
 * @throws IOException If an IO error occurs
 * @todo #1952:30min Right now we are displaying all languages for all
 *  repositories. We should only display the top 4 languages (ranked by
 *  bytes of code, as returned by Github) across all project repos.
 */
Set<String> languages(Github github, Iterable<String> repos) throws IOException {
  Set<String> langs = [] as Set<String>
  for (String repo : repos) {
    for (final Language lang : github.repos()
      .get(new Coordinates.Simple(repo)).languages()) {
      langs.add(lang.name())
    }
  }
  langs
}