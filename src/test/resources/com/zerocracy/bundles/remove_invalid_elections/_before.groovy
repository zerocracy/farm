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
package com.zerocracy.bundles.remove_invalid_elections

import com.jcabi.github.Github
import com.jcabi.github.Repo
import com.jcabi.github.Repos
import com.jcabi.xml.XML
import com.zerocracy.Farm
import com.zerocracy.Project
import com.zerocracy.entry.ExtGithub
import com.zerocracy.pm.in.Orders
import com.zerocracy.pm.scope.Wbs
import com.zerocracy.pm.staff.Elections
import com.zerocracy.pm.staff.Votes
import org.cactoos.map.MapEntry
import org.cactoos.map.MapOf

def exec(Project project, XML xml) {
  Farm farm = binding.variables.farm
  Github github = new ExtGithub(farm).value()
  Repo repo = github.repos().create(new Repos.RepoCreate('test', false))
  repo.issues().create('Hello, world', '')
  Elections elections = new Elections(project).bootstrap()
  String performer = 'performer'
  elections.elect(
    'gh:test/test#1',
    [performer],
    new MapOf<Votes, Integer>(new MapEntry<Votes, Integer>(new Votes.Fake(0.0D), -1))
  )
  elections.elect(
    'gh:test/test#2',
    [performer],
    new MapOf<Votes, Integer>(new MapEntry<Votes, Integer>(new Votes.Fake(1.0D), 1))
  )
  new Orders(project).bootstrap().assign('gh:test/test#2', performer , 1L)
  elections.elect(
    'gh:test/test#3',
    [performer],
    new MapOf<Votes, Integer>(new MapEntry<Votes, Integer>(new Votes.Fake(1.0D), 1))
  )
  new Wbs(project).bootstrap().remove('gh:test/test#3')
}
