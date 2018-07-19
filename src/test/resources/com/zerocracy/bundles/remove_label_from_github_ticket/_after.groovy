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
package com.zerocracy.bundles.remove_label_from_github_ticket

import com.jcabi.github.Coordinates
import com.jcabi.github.Issue
import com.jcabi.xml.XML
import com.zerocracy.Farm
import com.zerocracy.Project
import com.zerocracy.entry.ExtGithub
import org.cactoos.collection.Mapped
import org.cactoos.list.ListOf
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.hamcrest.core.IsEqual

def exec(Project project, XML xml) {
  Farm farm = binding.variables.farm
  Issue issue = new ExtGithub(farm).value().repos().get(
    new Coordinates.Simple('test', 'test')
  ).issues().get(1)
  MatcherAssert.assertThat(
    'Incorrect issue is being checked',
    new Issue.Smart(issue).title(),
    new IsEqual<String>('A bug')
  )
  MatcherAssert.assertThat(
    'Issue may have just a single label',
    new ListOf<>(
      new Mapped<>(
        { label -> label.name() },
        issue.labels().iterate()
      )
    ),
    Matchers.contains('bug')
  )
}
