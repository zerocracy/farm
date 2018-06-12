package com.zerocracy.bundles.set_agenda_title_from_github

import com.jcabi.xml.XML
import com.zerocracy.Farm
import com.zerocracy.Project
import com.zerocracy.pmo.Agenda
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers

def exec(Project project, XML xml) {
  MatcherAssert.assertThat(
    new Agenda(binding.variables.farm, 'anotheruser')
    .bootstrap()
    .title('gh:someuser/somerepo#1'),
    Matchers.equalTo('Github Issue Title')
  )
}
