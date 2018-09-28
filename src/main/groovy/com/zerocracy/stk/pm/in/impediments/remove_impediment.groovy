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
package com.zerocracy.stk.pm.in.impediments

import com.jcabi.xml.XML
import com.zerocracy.Farm
import com.zerocracy.Par
import com.zerocracy.Project
import com.zerocracy.entry.ClaimsOf
import com.zerocracy.farm.Assume
import com.zerocracy.claims.ClaimIn
import com.zerocracy.pm.in.Impediments

/**
 * This stakeholder responds to 'Remove impediment' event, which is
 * triggered when the user says "@0crat continue" on an issue that is
 * on hold.
 * @todo #1380:30min The impediment should also be removed from the Agenda. We
 *  should implement a mechanism similar to how register_impediment.groovy and
 *  add_impediment_to_agenda.groovy are working. This stakeholder should send a
 *  'Impediment was removed' claim, caught and handled by
 *  remove_impediment_from_agenda.groovy.
 */
def exec(Project project, XML xml) {
  new Assume(project, xml).notPmo()
  new Assume(project, xml).type('Remove impediment')
  ClaimIn claim = new ClaimIn(xml)
  String job = claim.param('job')
  String author = claim.author()
  Farm farm = binding.variables.farm
  new Impediments(farm, project)
    .bootstrap()
    .remove(job)
  claim.reply(
    new Par('@%s continued working on job %s').say(author, job)
  ).postTo(new ClaimsOf(farm, project))
}
