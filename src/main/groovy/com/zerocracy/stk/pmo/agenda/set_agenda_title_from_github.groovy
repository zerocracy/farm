package com.zerocracy.stk.pmo.agenda

import com.jcabi.github.Github
import com.jcabi.github.Issue
import com.jcabi.xml.XML
import com.zerocracy.Farm
import com.zerocracy.Project
import com.zerocracy.entry.ExtGithub
import com.zerocracy.farm.Assume
import com.zerocracy.claims.ClaimIn
import com.zerocracy.pmo.Agenda
import com.zerocracy.radars.github.Job

def exec(Project project, XML xml) {
  new Assume(project, xml).notPmo()
  new Assume(project, xml).type('Agenda was updated')
  ClaimIn claim = new ClaimIn(xml)
  if (!claim.hasParam('job')) {
    return
  }
  String job = claim.param('job')
  if (!job.startsWith('gh:')) {
    return
  }
  Farm farm = binding.variables.farm
  Agenda agenda = new Agenda(farm, claim.param('login')).bootstrap()
  if (agenda.exists(job) && agenda.title(job) == '-') {
    Github github = new ExtGithub(farm).value()
    Issue.Smart issue = new Issue.Smart(new Job.Issue(github, job))
    agenda.title(
      job,
      issue.title()
    )
  }
}
