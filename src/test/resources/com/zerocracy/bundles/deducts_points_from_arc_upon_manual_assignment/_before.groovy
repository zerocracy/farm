/*
 * Copyright (c) 2016-2018 Zerocracy
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the 'Software'), to read
 * the Software only. Permissions is hereby NOT GRANTED to use, copy, modify,
 * merge, publish, distribute, sublicense, and/or sell copies of the Software.
 *
 * THE SOFTWARE IS PROVIDED 'AS IS', WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.zerocracy.bundles.deducts_points_from_arc_upon_manual_assignment

import com.jcabi.github.*
import com.jcabi.xml.XML
import com.zerocracy.Farm
import com.zerocracy.Project
import com.zerocracy.entry.ExtGithub
import com.zerocracy.farm.fake.FkFarm
import com.zerocracy.radars.github.RbOnAssign

import javax.json.Json

def exec(Project project, XML xml) {
  Farm farm = binding.variables.farm
  Github github = new ExtGithub(farm).value()
  Repo repo = github.repos().create(new Repos.RepoCreate('test', false))
  Issue issue = new Issue.Smart(repo.issues().create('hello, world', ''))
  issue.assign('carlosmiranda')
  repo.issueEvents().create(
    Event.ASSIGNED, issue.number(), 'yegor256',
    com.google.common.base.Optional.absent()
  )
  new RbOnAssign().react(
    new FkFarm(project),
    github,
    Json.createObjectBuilder()
      .add('issue', Json.createObjectBuilder().add('number', issue.number()))
      .add('repository', Json.createObjectBuilder().add('full_name', repo.coordinates().toString()))
      .add('sender', Json.createObjectBuilder().add('login', 'yegor256'))
      .add('assignee', Json.createObjectBuilder().add('login', 'yegor256'))
      .build()
  )
}
