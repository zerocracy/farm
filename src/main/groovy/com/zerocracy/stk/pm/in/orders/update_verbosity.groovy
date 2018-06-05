/**
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
package com.zerocracy.stk.pm.in.orders

import com.jcabi.github.Comment
import com.jcabi.github.Github
import com.jcabi.github.Issue
import com.jcabi.xml.XML
import com.zerocracy.Farm
import com.zerocracy.Project
import com.zerocracy.entry.ExtGithub
import com.zerocracy.farm.Assume
import com.zerocracy.pm.ClaimIn
import com.zerocracy.pm.in.Orders
import com.zerocracy.pmo.Pmo
import com.zerocracy.pmo.Verbosity
import com.zerocracy.radars.github.Job
import org.cactoos.iterable.Filtered
import org.cactoos.iterable.LengthOf

// @todo #783:30min Commit messages are not counted. We have to find commits
//  belongs to issue and pushed by task performer and consider it as a
//  verbosity of a user.
def exec(Project project, XML xml) {
  new Assume(project, xml).notPmo()
  new Assume(project, xml).type('Close job')
  Farm farm = binding.variables.farm
  Github github = new ExtGithub(farm).value()
  ClaimIn claim = new ClaimIn(xml)
  String job = claim.param('job')
  String performer = new Orders(project).bootstrap().performer(job)
  Issue.Smart issue = new Issue.Smart(new Job.Issue(github, job))
  int verbosity = new LengthOf<>(
    new Filtered<>(
      { Comment cmt -> (new Comment.Smart(cmt).author().login() == performer) },
      issue.comments().iterate(new Date(0))
    )
  ).value()
  new Verbosity(new Pmo(farm), performer).bootstrap().add(job, project, verbosity)
}
