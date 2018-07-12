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
package com.zerocracy.bundles.bootstraps_a_project

import com.jcabi.xml.XML
import com.zerocracy.Farm
import com.zerocracy.Project
import com.zerocracy.pmo.Catalog
import com.zerocracy.pmo.Projects
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers

def exec(Project project, XML xml) {
  Farm farm = binding.variables.farm
  MatcherAssert.assertThat(
    'User projects updated',
    new Projects(farm, 'yegor256').bootstrap().iterate(),
    Matchers.hasItem(project.pid())
  )
  Catalog catalog = new Catalog(farm).bootstrap()
  MatcherAssert.assertThat(
    'Project title should be set from channel prop',
    catalog.title(project.pid()),
    Matchers.equalTo('Test')
  )
  MatcherAssert.assertThat(
    'Slack links is invalid',
    catalog.links(project.pid(),'slack'),
    Matchers.contains(Matchers.equalTo(project.pid()))
  )
}
