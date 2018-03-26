package com.zerocracy.stk.pmo.agenda

import com.jcabi.xml.XML
import com.zerocracy.Project
import com.zerocracy.farm.Assume
import com.zerocracy.pm.ClaimIn
import com.zerocracy.pm.in.Orders
import com.zerocracy.pmo.Agenda
import com.zerocracy.pmo.People
import com.zerocracy.pmo.Projects
import org.cactoos.iterator.Shuffled

def exec(Project pmo, XML xml) {
  new Assume(pmo, xml).isPmo()
  new Assume(pmo, xml).type('Ping hourly')
  ClaimIn claim = new ClaimIn(xml)
  People people = new People(pmo).bootstrap()
  new Shuffled<>(people.iterate().iterator()).take(5).each { login ->
    Agenda agenda = new Agenda(pmo, login).bootstrap()
    Projects projects = new Projects(pmo, login).bootstrap()
    Set<String> orders = []
    projects.iterate().each { project ->
      orders.addAll(new Orders(project).bootstrap().jobs(login))
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
        .postTo(pmo)
    }
  }
}
