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
 * @todo 83:30min We should add new "bans" to this XML document every time
 *  we add a new job to the WBS. Lets create a new stakeholder
 *  pm.staff.bans.ban_github_reporter. It has to catch "Job was added to WBS"
 *  type, check whether the job starts with gh: and then take the author of
 *  the ticket from GitHub.
 */
def exec(Project project, XML xml) {
  final job = 'gh:test/farm#1'
  final elections = new Elections(project).bootstrap()
  assert elections.elected(job)
//  assert elections.winner(job) == 'g4s8'
//  assert elections.reason(job) == 'reason'
}
