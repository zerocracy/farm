/*
 * Copyright (c) 2016-2018 Zerocracy
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to read
 * the Software only. Permissions is hereby NOT GRANTED to use, copy, modify,
 * merge, publish, distribute, sublicense, and/or sell copies of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.zerocracy.stk.pm.staff

import com.jcabi.xml.XML
import com.zerocracy.Project
import com.zerocracy.farm.Assume

def exec(Project project, XML xml) {
  new Assume(project, xml).notPmo()
  new Assume(project, xml).type('ignore')
  // @todo #1396:30min This stakeholder was disabled, because `Elections` class
  //  was removed. Let's find another way to check the lack of staff to
  //  notify a project about it.
//  ClaimIn claim = new ClaimIn(xml)
//  Wbs wbs = new Wbs(project).bootstrap()
//  Orders orders = new Orders(project).bootstrap()
//  Elections elections = new Elections(project).bootstrap()
//  List<String> pending = []
//  elections.jobs().each { job ->
//    Date created = wbs.created(job)
//    if (created.time > System.currentTimeMillis() - TimeUnit.DAYS.toMillis(2L)) {
//      return
//    }
//    if (orders.assigned(job)) {
//      return
//    }
//    if (elections.result(job).elected()) {
//      return
//    }
//    pending.add(job)
//  }
//  if (pending.empty) {
//    return
//  }
//  Roles roles = new Roles(project).bootstrap()
//  Farm farm = binding.variables.farm
//  People people = new People(farm).bootstrap()
//  int vacation = roles.everybody().count { uid -> people.vacation(uid) }
//  new Hint(
//    farm,
//    (int) TimeUnit.DAYS.toSeconds(5L),
//    claim.copy()
//      .type('Notify project')
//      .token("project;${project.pid()}")
//      .param('mnemo', 'Deficit of people')
//      .param(
//      'message',
//      new Par(
//        'There are %d jobs, which are not assigned to anyone: %s;',
//        'most likely there is a deficit of people in the project;',
//        'there are [%d people](/a/%s?a=pm/staff/roles) in the project now',
//        '(%d are on vacation);',
//        'consider announcing your project as explained in §51'
//      ).say(pending.size(), pending.join(', '), roles.everybody().size(), project.pid(), vacation)
//    )
//  ).postTo(project)
}
