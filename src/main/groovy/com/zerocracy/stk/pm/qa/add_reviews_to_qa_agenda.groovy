package com.zerocracy.stk.pm.qa


import com.jcabi.xml.XML
import com.zerocracy.Farm
import com.zerocracy.Project
import com.zerocracy.claims.ClaimIn
import com.zerocracy.entry.ClaimsOf
import com.zerocracy.farm.Assume
import com.zerocracy.pm.qa.Reviews
import com.zerocracy.pm.staff.Roles
import com.zerocracy.pmo.Agenda
import org.cactoos.collection.Filtered

def exec(Project project, XML xml) {
  new Assume(project, xml).notPmo()
  new Assume(project, xml).type('Ping daily')
  Farm farm = binding.variables.farm
  new Roles(project).bootstrap().findByRole('QA').each { login ->
    List<String> reviews = new Reviews(project).bootstrap().findByInspector(login)
    Agenda agenda = new Agenda(farm, login).bootstrap()
    Collection<String> jobs = agenda.jobs()
    boolean updated = false
    new Filtered<>({job -> !jobs.contains(job)}, reviews).each { job ->
      agenda.add(project, job, 'QA')
      updated = true
    }
    if (updated) {
      new ClaimIn(xml).copy()
        .type('Agenda was updated')
        .param('login', login)
        .postTo(new ClaimsOf(farm))
    }
  }
}
