package com.zerocracy.stk.pm.time.reminders

import com.jcabi.xml.XML
import com.zerocracy.farm.Assume
import com.zerocracy.jstk.Project
import com.zerocracy.pm.ClaimIn
import com.zerocracy.pm.in.Orders

def exec(Project project, XML xml) {
  new Assume(project, xml).type('New reminder posted')

  def claim = new ClaimIn(xml)
  def label = claim.param('label')
  def labelMatcher = (label =~ /(\d+)\sdays/)
  if (labelMatcher.matches() && labelMatcher.group(1).toInteger() >= 10) {
    def orders = new Orders(project).bootstrap()
    orders.resign(claim.param('job'))
  }
}
