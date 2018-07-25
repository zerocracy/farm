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
package com.zerocracy.stk.pmo.resumes

import com.jcabi.xml.XML
import com.zerocracy.Farm
import com.zerocracy.Par
import com.zerocracy.Policy
import com.zerocracy.Project
import com.zerocracy.entry.ClaimsOf
import com.zerocracy.farm.Assume
import com.zerocracy.pm.ClaimIn
import com.zerocracy.pmo.People
import com.zerocracy.pmo.Resumes
import org.cactoos.iterable.Filtered
import org.cactoos.iterable.ItemAt
import org.cactoos.list.ListOf
import org.cactoos.list.Shuffled

/**
 * Assign examiner to resume. When new resume submitted and added to
 * {@code resumes.xml} it should be assigned to examiner which can
 * invite user or deny the resume.
 *
 * @todo #1146:30min New user should be invited only by examiner.
 *  We should check that user has a resume in resumes.xml and
 *  author of invite is his examiner also new users can be invited by PMO
 *  members (has any role in PMO project) without resumes check.
 *  On invite we should notify user and examiner and add +32 reputation
 *  points to examiner. Also examiner should be able to reject resume by
 *  saying `deny @username`.
 * @todo #1146:30min Test for this stakeholder is missed. We should verify
 *  that new resume can be assigned only to high-reputation user (>= 1024)
 *  and can't be assigned to users on vacation.
 */
def exec(Project project, XML xml) {
  new Assume(project, xml).isPmo()
  new Assume(project, xml).type('Ping hourly')
  ClaimIn claim = new ClaimIn(xml)
  Farm farm = binding.variables.farm
  Resumes resumes = new Resumes(farm).bootstrap()
  int reputation = new Policy(farm).get('1.min-rep', 1024)
  People people = new People(farm).bootstrap()
  List<String> examiners = new ListOf<>(
    new Filtered<String>(
      { people.reputation(it) > reputation && !people.vacation(it) },
      people.iterate()
    )
  )
  resumes.unassigned().each {
    String examiner = new ItemAt<String>(new Shuffled(examiners)).value()
    resumes.assign(it, examiner)
    claim.copy()
      .type('Notify user')
      .token("user;${examiner}")
      .param(
        'message',
        new Par(
          farm,
          'A new applicant, @%s, has been assigned to you.',
          'Please review the resume, and either invite or reject the applicant.',
          'In either case you will receive +32 reputation points as in §1'
        ).say(it)
      ).postTo(new ClaimsOf(farm, project))
  }
}
