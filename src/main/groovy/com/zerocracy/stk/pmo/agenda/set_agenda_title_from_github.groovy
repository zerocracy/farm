package com.zerocracy.stk.pmo.agenda

import com.jcabi.github.Github
import com.jcabi.github.Issue
import com.jcabi.xml.XML
import com.zerocracy.Farm
import com.zerocracy.Project
import com.zerocracy.entry.ExtGithub
import com.zerocracy.farm.Assume
import com.zerocracy.pm.ClaimIn
import com.zerocracy.pmo.Agenda
import com.zerocracy.radars.github.Job

def exec(Project project, XML xml) {
  new Assume(project, xml).notPmo()
  new Assume(project, xml).type('Start order')
  ClaimIn claim = new ClaimIn(xml)
  String job = claim.param('job')
  if (!job.startsWith('gh:')) {
    return
  }
  String login = claim.param('login')
  Farm farm = binding.variables.farm
  Agenda agenda = new Agenda(farm, login).bootstrap()
  if (agenda.exists(job)) {
    Github github = new ExtGithub(farm).value()
    Issue.Smart issue = new Issue.Smart(new Job.Issue(github, job))
    agenda.title(
      job,
      issue.title()
    )
  }
}
