/*
 * Copyright (c) 2016-2019 Zerocracy
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
package com.zerocracy.stk.pm.qa

import com.jcabi.xml.XML
import com.zerocracy.Farm
import com.zerocracy.Par
import com.zerocracy.Project
import com.zerocracy.cash.Cash
import com.zerocracy.entry.ClaimsOf
import com.zerocracy.farm.Assume
import com.zerocracy.claims.ClaimIn
import com.zerocracy.pm.qa.Reviews
import com.zerocracy.pm.staff.Roles
import com.zerocracy.pmo.Agenda
import com.zerocracy.pmo.People
import org.cactoos.collection.Filtered
import java.security.SecureRandom


// @todo #1904:30min 0crat often can't update agenda of performer on 'Start QA review'
//  because failed to find the job in agenda. 0crat is responding with message
//  "can't inspect" right in the ticket.
def exec(Project project, XML xml) {
  new Assume(project, xml).notPmo()
  new Assume(project, xml).type('Start QA review')
  Farm farm = binding.variables.farm
  ClaimIn claim = new ClaimIn(xml)
  String job = claim.param('job')
  String performer = claim.param('login')
  int minutes = Integer.parseInt(claim.param('minutes'))
  Cash cash = new Cash.S(claim.param('cash'))
  Cash bonus = new Cash.S(claim.param('bonus'))
  People people = new People(farm).bootstrap()
  Roles roles = new Roles(project).bootstrap()
  List<String> qaList = roles.findByRole('QA')
  Collection<String> qa =  new Filtered<String>({ uid -> !people.vacation(uid) }, qaList)
  if (qa.empty){
    String arc = roles.findByRole('ARC')[0]
    claim.reply(
      new Par(
  '@%s all QAs are on vacation. Please handle that.'
      ).say(arc)
    ).postTo(new ClaimsOf(farm, project))
    return
  }
  String inspector
  if (qa.size() > 1) {
    inspector = qa[new SecureRandom().nextInt(qa.size() - 1)]
  } else {
    inspector = qa.first()
  }
  Reviews reviews = new Reviews(project).bootstrap()
  reviews.add(job, inspector, performer, cash, minutes, bonus)

  new Agenda(farm, inspector).bootstrap().add(project, job, 'QA')
  claim.copy()
    .type('Agenda was updated')
    .param('login', inspector)
    .param('reason', 'start_qa_review')
    .postTo(new ClaimsOf(farm, project))
  claim.copy().type('Notify job').token("job;${job}").param(
    'message',
    new Par(
      '@%s please review this job completed by @%s, as in §30;',
      'the job will be fully closed and all payments will be made',
      'when the quality review is completed'
    ).say(inspector, performer)
  ).postTo(new ClaimsOf(farm, project))
  new Agenda(farm, performer).bootstrap().inspector(job, inspector)
}
