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
package com.zerocracy.bundles.close_job_with_quality_review

import com.jcabi.github.*
import com.jcabi.xml.XML
import com.zerocracy.Farm
import com.zerocracy.Project
import com.zerocracy.cash.Cash
import com.zerocracy.entry.ExtGithub
import com.zerocracy.pm.cost.Ledger
import com.zerocracy.pm.cost.Rates
import com.zerocracy.pmo.Awards
import com.zerocracy.pmo.Debts
import com.zerocracy.pmo.Projects
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers

def exec(Project project, XML xml) {
  Farm farm = binding.variables.farm
  Github github = new ExtGithub(farm).value()
  Repo repo = github.repos().create(new Repos.RepoCreate('test', false))
  new Issue.Smart(repo.issues().create('for test', '')).close()
  new Issue.Smart(
    new Pull.Smart(repo.pulls().create('PR', 'dev', 'mater')).issue()
  ).close()
  new Ledger(project).bootstrap().add(
    new Ledger.Transaction(
      new Cash.S('$1000'),
      'assets', 'cash',
      'income', 'zerocracy',
      'Donated by unknown'
    )
  )
  String coder = 'coder'
  String reviewer = 'reviewer'
  new Projects(farm, coder).bootstrap().add(project.pid())
  new Projects(farm, reviewer).bootstrap().add(project.pid())
  new Rates(project).bootstrap().with {
   set coder, new Cash.S('$100')
   set reviewer, new Cash.S('$200')
}
  [coder, reviewer].each {
    MatcherAssert.assertThat(
      new Awards(farm, it).bootstrap().total(),
      Matchers.is(0)
    )
    MatcherAssert.assertThat(
      new Debts(farm).bootstrap().exists(it),
      Matchers.is(false)
    )
  }
}
