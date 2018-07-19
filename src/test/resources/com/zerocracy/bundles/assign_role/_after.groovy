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
package com.zerocracy.bundles.assign_role

import com.jcabi.xml.XML
import com.zerocracy.Project
import com.zerocracy.pm.staff.Roles
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers

def exec(Project project, XML xml) {
  Roles roles = new Roles(project).bootstrap()
  MatcherAssert.assertThat(
    'DEV roles was not assigned to g4s8',
    roles.allRoles('g4s8'),
    Matchers.contains(Matchers.equalTo('DEV'))
  )
  // @todo #989:30min MkGithub can't create private repository,
  //  any repository from MkGithub is public, see _before in this test.
  //  I suppose it's bug in jcabi-github, see
  //  https://github.com/jcabi/jcabi-github/issues/1421
  //  When the bug is fixed, update jcabi-github and uncomment code below.
//  MatcherAssert.assertThat(
//    'DEV role was assigned to anonymous',
//    roles.allRoles('anonymous'),
//    Matchers.emptyIterable()
//  )
}
