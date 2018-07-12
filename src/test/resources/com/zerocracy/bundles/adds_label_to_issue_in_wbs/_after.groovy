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
package com.zerocracy.bundles.adds_label_to_issue_in_wbs

import com.jcabi.github.Github
import com.jcabi.github.Issue
import com.jcabi.github.IssueLabels
import com.jcabi.xml.XML
import com.zerocracy.Farm
import com.zerocracy.Project
import com.zerocracy.entry.ExtGithub
import com.zerocracy.pm.scope.Wbs
import com.zerocracy.radars.github.Job
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers

def exec(Project project, XML xml) {
  Wbs wbs = new Wbs(project).bootstrap()
  String wbsIn = 'gh:test/test#1'
  String wbsOut = 'gh:test/test#2'
  MatcherAssert.assertThat(
    'issue #1 is not in WBS',
    wbs.exists(wbsIn),
    Matchers.is(true)
  )
  MatcherAssert.assertThat(
    'issue #2 in WBS',
    wbs.exists(wbsOut),
    Matchers.is(false)
  )
  Farm farm = binding.variables.farm
  Github github = new ExtGithub(farm).value()
  Issue.Smart issueIn = new Issue.Smart(new Job.Issue(github, wbsIn))
  Issue.Smart issueOut = new Issue.Smart(new Job.Issue(github, wbsOut))
  MatcherAssert.assertThat(
    'Issue in WBS does not contain "scope" label',
    new IssueLabels.Smart(issueIn.labels()).contains('scope'),
    Matchers.is(true)
  )
  MatcherAssert.assertThat(
    'Issue not in WBS contains "scope" label',
    new IssueLabels.Smart(issueOut.labels()).contains('scope'),
    Matchers.is(false)
  )
}
