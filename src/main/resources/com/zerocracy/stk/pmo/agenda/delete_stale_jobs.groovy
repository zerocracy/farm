package com.zerocracy.stk.pmo.agenda

import com.jcabi.xml.XML
import com.zerocracy.Project
import com.zerocracy.farm.Assume

/**
 * @todo #496:30min Let's implement this stakeholder. It has to
 *  fetch all users from people.xml, pick random 5, go through their agendas
 *  and check that each job they have there really exist in the correspondent projects.
 *  If they don't exist, it should remove them from the agenda.
 *  Also uncomment 'delete_stale_jobs' test assertion.
 */
def exec(Project project, XML xml) {
  new Assume(project, xml).type('Ping hourly')
}
