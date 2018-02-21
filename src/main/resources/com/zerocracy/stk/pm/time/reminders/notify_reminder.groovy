package com.zerocracy.stk.pm.time.reminders

import com.jcabi.xml.XML
import com.zerocracy.Par
import com.zerocracy.Project
import com.zerocracy.farm.Assume
import com.zerocracy.pm.ClaimIn

def exec(Project project, XML xml) {
  new Assume(project, xml).notPmo()
  new Assume(project, xml).type('New reminder posted')
  ClaimIn claim = new ClaimIn(xml)
  claim.copy()
    .type('Notify job')
    .token("job;${claim.param('job')}")
    .param(
      'message',
      new Par(
        '@%s this job was assigned to you %s ago.',
        'It will be taken away from you soon, unless you close it, see §8.',
        'Read [this](/2014/04/13/no-obligations-principle.html)',
        'and [this](/2014/11/24/principles-of-bug-tracking.html), please.'
      ).say(claim.param('login'), claim.param('label'))
    ).postTo(project)
}
