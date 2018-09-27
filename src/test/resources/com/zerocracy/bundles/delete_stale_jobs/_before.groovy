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
package com.zerocracy.bundles.delete_stale_jobs

import com.jcabi.github.Issues
import com.jcabi.github.Repos
import com.jcabi.xml.XML
import com.zerocracy.Farm
import com.zerocracy.Project
import com.zerocracy.cash.Cash
import com.zerocracy.entry.ExtGithub
import com.zerocracy.pm.qa.Reviews
import com.zerocracy.pmo.Agenda

def exec(Project pmo, XML xml) {
  Farm farm = binding.variables.farm
  Project project = farm.find("@id='TESTPROJECT'")[0]
  Issues issues = new ExtGithub(farm).value().repos()
    .create(new Repos.RepoCreate('test', false))
    .issues()
  issues.create('hello1', 'world1')
  issues.create('hello2', 'world2')
  String user = 'g4s8'
  Agenda agenda = new Agenda(farm, user).bootstrap()
  agenda.add(project, 'gh:test/test#1', 'DEV')
  String inspected = 'gh:test/test#2'
  agenda.add(project, inspected, 'DEV')
  agenda.inspector(inspected, 'some-qa')
  new Reviews(project).bootstrap().add(inspected,'test', user, new Cash.S('$10'), 30, new Cash.S('$0'))
}
