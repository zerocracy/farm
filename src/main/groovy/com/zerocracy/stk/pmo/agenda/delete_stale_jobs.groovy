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

package com.zerocracy.stk.pmo.agenda

import com.jcabi.xml.XML
import com.zerocracy.Farm
import com.zerocracy.Project
import com.zerocracy.entry.ClaimsOf
import com.zerocracy.farm.Assume
import com.zerocracy.claims.ClaimIn
import com.zerocracy.pm.in.Orders
import com.zerocracy.pm.qa.Reviews
import com.zerocracy.pmo.Agenda
import com.zerocracy.pmo.People
import com.zerocracy.pmo.Projects
import org.cactoos.collection.Shuffled

def exec(Project pmo, XML xml) {
  new Assume(pmo, xml).isPmo()
  new Assume(pmo, xml).type('Ping hourly')
  ClaimIn claim = new ClaimIn(xml)
  Farm farm = binding.variables.farm
  People people = new People(farm).bootstrap()
  new Shuffled<String>(people.iterate()).take(10).each { login ->
    Agenda agenda = new Agenda(farm, login).bootstrap()
    Projects projects = new Projects(farm, login).bootstrap()
    Set<String> orders = []
    projects.iterate().each { pid ->
      if (pid == 'PMO') {
        return
      }
      orders.addAll(
        new Orders(farm, farm.find("@id='${pid}'")[0]).bootstrap().jobs(login)
      )
      orders.addAll(
        new Reviews(farm.find("@id='${pid}'")[0]).bootstrap().findByInspector(login)
      )
    }
    boolean updated = false
    agenda.jobs().each { job ->
      if (!orders.contains(job) && !agenda.hasInspector(job)) {
        agenda.remove(job)
        updated = true
      }
    }
    if (updated) {
      claim.copy()
        .type('Agenda was updated')
        .param('login', login)
        .postTo(new ClaimsOf(farm))
    }
  }
}
