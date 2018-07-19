package com.zerocracy.stk.pmo.agenda

import com.jcabi.xml.XML
import com.zerocracy.Farm
import com.zerocracy.Project
import com.zerocracy.farm.Assume
import com.zerocracy.pm.ClaimIn
import com.zerocracy.pm.in.Orders
import com.zerocracy.pm.staff.Roles
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
      if (
        new Roles(
            farm.find("@id='${pid}'").iterator().next()
        ).bootstrap().hasRole('QA')
      ) {
        return
        // @todo #1414:30min Cleanup of stale jobs is disabled for QA roles.
        //  This is because QA jobs are not in Orders, but in Reviews. This
        //  causes a bug where QA agenda is removed even though the jobs have
        //  not completed review yet. For QA roles, get the list of jobs from
        //  Reviews.findByInspector(). Let's also add a Bundles test case
        //  where delete_stale_jobs should clean up jobs that are not in
        //  Reviews and retains those that are still awaiting verdict.
      }
      orders.addAll(
        new Orders(farm.find("@id='${pid}'")[0]).bootstrap().jobs(login)
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
        .postTo(pmo)
    }
  }
}
