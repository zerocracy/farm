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
package com.zerocracy.bundles.deny_resume_application

import com.jcabi.github.Repos
import com.jcabi.xml.XML
import com.zerocracy.Farm
import com.zerocracy.Project
import com.zerocracy.entry.ExtGithub
import com.zerocracy.pmo.Awards
import com.zerocracy.pmo.Resumes

import java.time.LocalDateTime

def exec(Project project, XML xml) {
  Farm farm = binding.variables.farm
  Resumes resumes = new Resumes(farm).bootstrap()
  String login = 'friend'
  resumes.add(
    login,
    LocalDateTime.now(),
    'User description on himself',
    'INTP-A',
    1000541,
    'telegramFriend'
  )
  String user = 'user'
  resumes.assign(login, user)
  new ExtGithub(farm).value().repos().create(new Repos.RepoCreate('test', false))
  new Awards(farm, user).bootstrap().add(
    project, 1000, 'gh:test/test#1', 'test'
  )
}
