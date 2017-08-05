/**
 * Copyright (c) 2016-2017 Zerocracy
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
package com.zerocracy.stk.pm.scope.wbs

import com.jcabi.xml.XML
import com.zerocracy.farm.Assume
import com.zerocracy.jstk.Project
import com.zerocracy.pm.scope.Wbs
import com.zerocracy.pm.staff.Elections

/**
 * @todo #145:30m Needs project cross-item synchronization.
 *  Now WBS can be changed during elections cleanup that
 *  resulting in keeping unused elections until next WBS update.
 *  To avoid this behavior we need some kind of transactional items change.
 */
def exec(Project project, XML xml) {
  new Assume(project, xml).types(['Add job to WBS', 'Remove job from WBS'])
  final jobs = new Wbs(project).bootstrap().iterate().toList()
  final elections = new Elections(project).bootstrap()
  elections.jobs().each {
    if (!jobs.contains(it)) {
      elections.remove(it)
    }
  }
}