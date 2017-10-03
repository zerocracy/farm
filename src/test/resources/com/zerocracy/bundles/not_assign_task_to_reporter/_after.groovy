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
package com.zerocracy.bundles.not_assign_task_to_reporter

import com.jcabi.xml.XML
import com.zerocracy.jstk.Project
import com.zerocracy.pm.staff.Elections
/**
 * @todo #93:30min Task reporter is already added to bans.xml. However, this
 *  test still fails for some reason. It could be because the election
 *  mechanism is broken (https://github.com/zerocracy/farm/issues/150), or that
 *  the test setup or data is not complete. Let's find out what's wrong and
 *  revise the test appropriately, perhaps even rewriting it.
 */
def exec(Project project, XML xml) {
  new Elections(project).bootstrap()
  // @todo #258:30m This assertion `elections.elected(job)` always failing
  //  on Rultor but passing locally and on Shippable. It related to #258 bug.
  //  This `assert` should be uncommented after #258 fix.
//  String job = 'gh:test/farm#1'
//  assert elections.elected(job)
//  assert elections.winner(job) == 'g4s8'
//  assert elections.reason(job) == 'reason'
}
