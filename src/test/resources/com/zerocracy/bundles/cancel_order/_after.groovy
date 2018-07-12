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
package com.zerocracy.bundles.cancel_order

import com.jcabi.github.*
import com.jcabi.xml.XML
import com.zerocracy.Farm
import com.zerocracy.Project
import com.zerocracy.entry.ExtGithub
import com.zerocracy.pm.staff.Bans
import com.zerocracy.pmo.Agenda
import org.cactoos.collection.Mapped
import org.cactoos.list.ListOf
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers

def exec(Project project, XML xml) {
  Farm farm = binding.variables.farm
  Github github = new ExtGithub(farm).value()
  Repo repo = github.repos().get(new Coordinates.Simple('test/test'))
  Issue.Smart issue = new Issue.Smart(repo.issues().get(1))
  MatcherAssert.assertThat(
    issue.assignee().login(),
    Matchers.equalTo('')
  )
  MatcherAssert.assertThat(
    new Agenda(farm, 'g4s8').bootstrap().exists('gh:test/test#1'),
    Matchers.equalTo(false)
  )
  MatcherAssert.assertThat(
    new Bans(project).bootstrap().reasons('gh:test/test#1', 'g4s8'),
    Matchers.iterableWithSize(1)
  )
  MatcherAssert.assertThat(
    new Mapped<>(
      {new Comment.Smart(it as Comment).body() },
      issue.comments().iterate(new Date(0))
    ),
    Matchers.hasItem(Matchers.stringContainsInOrder(new ListOf<String>('please stop working', 'Test reason')))
  )
}
