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
        new Orders(farm.find("@id='${pid}'")[0]).bootstrap().jobs(login)
      )
      orders.addAll(
        new Reviews(farm.find("@id='${pid}'")[0]).bootstrap().findByInspector(login)
      )
    }
    boolean updated = false
    agenda.jobs().each { job ->
      if (!orders.contains(job)) {
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
