package com.zerocracy.bundles.set_agenda_title_from_github

import com.jcabi.xml.XML
import com.zerocracy.Farm
import com.zerocracy.Project
import com.zerocracy.pmo.Agenda
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers

def exec(Project project, XML xml) {
  Farm farm = binding.variables.farm
  Agenda agenda = new Agenda(farm, 'anotheruser').bootstrap()
  MatcherAssert.assertThat(
    agenda.title('gh:someuser/somerepo#1'),
    Matchers.equalTo('Github Issue Title')
  )
}