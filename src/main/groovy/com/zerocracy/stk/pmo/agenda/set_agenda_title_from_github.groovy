package com.zerocracy.stk.pmo.agenda

import com.jcabi.xml.XML
import com.zerocracy.Project
import com.zerocracy.farm.Assume
import com.zerocracy.pm.ClaimIn
import com.zerocracy.pmo.Agenda

def exec(Project project, XML xml) {
    new Assume(project, xml).notPmo()
    new Assume(project, xml).type('Set order title from github')
    ClaimIn claim = new ClaimIn(xml)
    String job = claim.param('job')
    String login = claim.param('login')
    Agenda agenda = new Agenda(binding.variables.farm, login).bootstrap()
    if (agenda.exists(job)) {
        agenda.title(
            job,
            'Github Issue Title'
        )
    }
}
