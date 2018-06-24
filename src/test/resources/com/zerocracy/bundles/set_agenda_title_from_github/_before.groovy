package com.zerocracy.bundles.set_agenda_title_from_github

import com.jcabi.github.Repos
import com.jcabi.xml.XML
import com.zerocracy.Farm
import com.zerocracy.Project
import com.zerocracy.entry.ExtGithub
import com.zerocracy.pmo.Agenda

def exec(Project project, XML xml) {
  Farm farm = binding.variables.farm
  new ExtGithub(farm).value().repos()
      .create(new Repos.RepoCreate('somerepo', false))
      .issues()
      .create('Github Issue Title', 'Github Issue Text')
  new Agenda(farm, 'anotheruser').bootstrap()
      .add(
          project,
          'gh:someuser/somerepo#1',
          'DEV'
      )
}
