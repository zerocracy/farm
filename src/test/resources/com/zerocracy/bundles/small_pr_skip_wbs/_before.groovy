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
package com.zerocracy.bundles.small_pr_skip_wbs

import com.jcabi.github.Github
import com.jcabi.github.Pull
import com.jcabi.github.Repo
import com.jcabi.github.Repos
import com.jcabi.xml.XML
import com.zerocracy.Farm
import com.zerocracy.Project
import com.zerocracy.entry.ExtGithub
import com.zerocracy.farm.fake.FkFarm
import com.zerocracy.radars.github.RbOnPullRequest

import javax.json.Json

def exec(Project project, XML xml) {
  Farm farm = binding.variables.farm
  Github github = new ExtGithub(farm).value()
  Repo repo = github.repos().create(new Repos.RepoCreate('test', false))
  Pull pull = repo.pulls().create('New Small PR', 'master', 'master')
  pull.patch(
    Json.createObjectBuilder()
    .add('additions', 5).add('deletions', 4).build()
  )
  new RbOnPullRequest().react(
    new FkFarm(project),
    github,
    Json.createObjectBuilder()
      .add('pull_request', Json.createObjectBuilder().add('number', pull.number()))
      .add('repository', Json.createObjectBuilder().add('full_name', repo.coordinates().toString()))
      .build()
  )
}
