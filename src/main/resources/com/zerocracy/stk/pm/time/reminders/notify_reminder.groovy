package com.zerocracy.stk.pm.time.reminders

import com.jcabi.xml.XML
import com.zerocracy.farm.Assume
import com.zerocracy.jstk.Project
import com.zerocracy.pm.ClaimIn
import com.zerocracy.pm.ClaimOut

def exec(Project project, XML xml) {
  new Assume(project, xml).notPmo()
  new Assume(project, xml).type('New reminder posted')
  def claim = new ClaimIn(xml)
  new ClaimOut()
    .type('Notify job')
    .token("job;${claim.param('job')}")
    .param(
      'message',
      "@${claim.param('login')} this job was assigned to you"
        + " ${claim.param('label')} ago."
        + ' It will be taken away from you soon, unless you close it,'
        + ' see [§8](http://datum.zerocracy.com/pages/policy.html#8).'
        + ' Read [this](http://www.yegor256.com/2014/04/13/no-obligations-principle.html)'
        + ' and [this](http://www.yegor256.com/2014/11/24/principles-of-bug-tracking.html), please.'
    ).postTo(project)
}
