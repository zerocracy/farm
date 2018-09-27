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
package com.zerocracy.bundles.remove_from_agenda_on_repo_rename

import com.jcabi.github.Repo
import com.jcabi.github.Repos
import com.jcabi.xml.XML
import com.zerocracy.Farm
import com.zerocracy.Project
import com.zerocracy.entry.ExtGithub
import com.zerocracy.pm.in.Orders
import com.zerocracy.pm.scope.Wbs
import com.zerocracy.pmo.Agenda
import com.zerocracy.pmo.Projects

import javax.json.Json

def exec(Project project, XML xml) {
  Farm farm = binding.variables.farm
  Repo repo = new ExtGithub(farm).value().repos().create(new Repos.RepoCreate('repo', false))
  Projects prjs = new Projects(farm, 'g4s8').bootstrap()
  prjs.add('ZEROCRACY')
  Project prj = farm.find("@id='ZEROCRACY'")[(0)]
  repo.issues().create('hello', 'world')
  new Wbs(prj).bootstrap().add('gh:test/repo#1')
  new Orders(farm, prj).bootstrap().assign('gh:test/repo#1','g4s8',5)
  new Agenda(farm, 'g4s8').bootstrap().add(project, 'gh:test/repo#1', 'DEV')
  repo.patch(Json.createObjectBuilder().add('name', 'other').build())
}
