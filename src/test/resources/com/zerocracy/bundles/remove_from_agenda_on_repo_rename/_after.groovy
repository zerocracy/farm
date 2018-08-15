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
package com.zerocracy.bundles.remove_from_agenda_on_repo_rename

import com.jcabi.xml.XML
import com.zerocracy.Project

// @todo #1077:30min Job is not removed from agenda on repo rename. Some jobs
//  are not being removed from agenda. In one of the cases, when a repo is
//  renamed the jobs are not removed from the agenda. After fixing this
//  problem uncomment the test below to assure that the job is being
//  correctly removed
def exec(Project project, XML xml) {
//  Farm farm = binding.variables.farm
//  Agenda agenda = new Agenda(farm, 'g4s8').bootstrap()
//  MatcherAssert.assertThat(
//    'Did not removed item from agenda on repository rename',
//    agenda.jobs(),
//    new IsEmptyIterable<>()
//  )
}
