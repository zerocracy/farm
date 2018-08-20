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
import com.mongodb.client.model.Filters
import com.zerocracy.Farm
import com.zerocracy.Project
import com.zerocracy.claims.Footprint
import com.zerocracy.pm.staff.Roles
import org.cactoos.text.JoinedText
import org.hamcrest.MatcherAssert
import org.hamcrest.collection.IsEmptyIterable
import org.hamcrest.core.IsCollectionContaining
import org.hamcrest.core.IsEqual
import org.hamcrest.core.IsNot

def exec(Project project, XML xml) {
  Farm farm = binding.variables.farm
  Roles roles = new Roles(project).bootstrap()
  MatcherAssert.assertThat(
    'DEV role was not assigned to g4s8',
    roles.allRoles('g4s8'),
    new IsCollectionContaining<>(
      new IsEqual('DEV')
    )
  )
  MatcherAssert.assertThat(
    'REV role was not assigned to g4s8',
    roles.allRoles('g4s8'),
    new IsCollectionContaining<>(
      new IsEqual('REV')
    )
  )
  MatcherAssert.assertThat(
    'REV role was assigned to yegor256',
    roles.allRoles('yegor256'),
    new IsNot<>(
        new IsCollectionContaining<>(
          new IsEqual('REV')
        )
    )
  )
  MatcherAssert.assertThat(
    'Messages not sent to ARC',
    new Footprint(farm, project).collection().find(
      Filters.and(
        Filters.eq('project', project.pid()),
        Filters.eq('type', 'Notify test'),
        Filters.or(
          Filters.eq(
            'message',
            new JoinedText(
              '',
              'Role `DEV` was successfully assigned to @g4s8[/z]',
              '(https://www.0crat.com/u/g4s8), see [full list]',
              '(https://www.0crat.com/a/ASSIGNROL?a=pm/staff/roles) of roles; ',
              'hourly rate of @g4s8[/z](https://www.0crat.com/u/g4s8) is not ',
              'set, the user will receive no money for task completion; ',
              'if you want to assign an hourly rate, say `assign DEV g4s8 $25`',
              ', for example'
            ).asString()
          ),
          Filters.eq(
            'message',
            new JoinedText(
              '',
              'Role `REV` was successfully assigned to @g4s8[/z]',
              '(https://www.0crat.com/u/g4s8), see [full list]',
              '(https://www.0crat.com/a/ASSIGNROL?a=pm/staff/roles) of roles; ',
              'If you don\'t want this user as \'REV\', use ',
              '`resign REV` command; ',
              'hourly rate of @g4s8[/z](https://www.0crat.com/u/g4s8) is not ',
              'set, the user will receive no money for task completion; ',
              'if you want to assign an hourly rate, say `assign REV g4s8 $25`',
              ', for example'
            ).asString()
          )
        )
      )
    ),
    new IsNot<>(
      new IsEmptyIterable()
    )
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
