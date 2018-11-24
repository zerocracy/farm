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
package com.zerocracy.stk.internal

import com.jcabi.github.Github
import com.jcabi.log.Logger
import com.jcabi.xml.XML
import com.zerocracy.Farm
import com.zerocracy.Project
import com.zerocracy.entry.ExtDynamo
import com.zerocracy.entry.ExtGithub
import com.zerocracy.farm.Assume
import com.zerocracy.farm.Errors
import com.zerocracy.farm.props.Props
import com.zerocracy.radars.github.Quota

// @todo #1570:30min cleanup_comments often fails. This happens because we
//  are trying to remove from github some comment that had already been
//  deleted before using github interface. We must test if the comment exists
//  in github before trying to delete it. After the correction, uncomment
//  tests in /cleanup_comments/_after.groovy. Don't forget to remove the lines
//  that skip this stakeholder in test mode too so we can test its behavior.
def exec(Project project, XML xml) {
  new Assume(project, xml).isPmo()
  new Assume(project, xml).type('Ping hourly')
  Farm farm = binding.variables.farm
  if (new Props(farm).has('//testing')) {
    Logger.info(this, 'skip in testing mode')
    return
  }
  Github github = new ExtGithub(farm).value()
  if (!new Quota(github).quiet()) {
    return
  }
  Errors.Github errors = new Errors.Github(
    new Errors(new ExtDynamo(farm).value()),
    github
  )
  errors.iterate(10, 72L).each { error ->
    errors.remove(error)
    error.remove()
  }
}
