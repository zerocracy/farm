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
package com.zerocracy.bundles.notify_in_github

import com.jcabi.github.Repos
import com.zerocracy.ext.ExtGithub
import com.zerocracy.jstk.Project
import com.zerocracy.pm.ClaimOut

def exec(Project project) {
  def github = new ExtGithub(project).asValue()
  def repo = github.repos().create(new Repos.RepoCreate("test/test", false))
  def issue = repo.issues().create("hello, world", "")
  println 'hello'
  new ClaimOut()
    .type("hello")
    .token("github;${repo.coordinates()};${issue.number()}")
    .postTo(project)
}
