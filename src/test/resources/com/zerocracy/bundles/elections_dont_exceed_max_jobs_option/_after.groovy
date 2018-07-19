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
package com.zerocracy.bundles.elections_dont_exceed_max_jobs_option

import com.jcabi.xml.XML
import com.zerocracy.Project

def exec(Project project, XML xml) {
  // @todo #1035:30min Options are not used. Election should check
  //  maxJobsInAgenda option and not elect users that already have already
  //  max number of jobs. After this is implemented uncomment this test.

//  String job = 'gh:test/test#1'
//  Exception exception = null
//  try {
//    new Orders(project).bootstrap().orders.performer(job)
//  } catch (SoftException ex) {
//    exception = ex
//    assert exception.getMessage() == String.format(
//      'Job `%s` is not assigned, can\'t get performer', job
//    )
//  }
//  assert exception != null
//  Farm farm = binding.variables.farm
//  assert new LengthOf(
//    new Footprint(farm, project).collection().find(
//      Filters.and(
//        Filters.eq('project', project.pid()),
//        Filters.eq('type', 'Performer was elected')
//      )
//    )
//  ).value() == 0
}
