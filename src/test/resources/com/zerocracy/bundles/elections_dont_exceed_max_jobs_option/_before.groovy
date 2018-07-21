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

import com.jcabi.github.Github
import com.jcabi.github.Repo
import com.jcabi.github.Repos
import com.jcabi.xml.XML
import com.zerocracy.Farm
import com.zerocracy.Project
import com.zerocracy.entry.ExtGithub
import com.zerocracy.pm.staff.Roles
import com.zerocracy.pmo.Agenda
import com.zerocracy.pmo.Options
import com.zerocracy.pmo.Pmo
import com.zerocracy.pmo.Projects
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers

def exec(Project project, XML xml) {
  Farm farm = binding.variables.farm
  Github github = new ExtGithub(farm).value()
  Repo repo = github.repos().create(new Repos.RepoCreate('test', false))
  repo.issues().create('Hello, world1', '')
  Pmo pmo = new Pmo(farm)
  String user = 'yegor256'
  new Options(pmo, user).bootstrap().maxJobsInAgenda(1)
  new Agenda(pmo, user).bootstrap().add(project, 'none', 'DEV')
  new Roles(project).bootstrap().assign(user, 'DEV')
  new Projects(farm, user).bootstrap().add(project.pid())
  MatcherAssert.assertThat(
    new Agenda(farm, 'yegor256').bootstrap().jobs(),
    Matchers.hasSize(1)
  )
}
