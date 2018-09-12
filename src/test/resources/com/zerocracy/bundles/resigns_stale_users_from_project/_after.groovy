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
package com.zerocracy.bundles.resigns_stale_users_from_project

import com.jcabi.xml.XML
import com.zerocracy.Project
import com.zerocracy.pm.staff.Roles
import org.hamcrest.MatcherAssert
import org.hamcrest.collection.IsEmptyCollection
import org.hamcrest.core.IsCollectionContaining
import org.hamcrest.core.IsEqual
import org.hamcrest.core.IsNot

def exec(Project project, XML xml) {
  Roles roles = new Roles(project).bootstrap()
  MatcherAssert.assertThat(
      'g4s8 is not found but not resigned from project',
      roles.allRoles('g4s8'),
      new IsEmptyCollection<>()
  )
  MatcherAssert.assertThat(
      'yegor256 was removed, even with role PO',
      roles.allRoles('yegor256'),
      new IsCollectionContaining<>(
          new IsEqual('PO')
      )
  )
  MatcherAssert.assertThat(
      'carlosmiranda is active and supposed to remain DEV',
      roles.allRoles('carlosmiranda'),
      new IsNot<>(
          new IsCollectionContaining<>(
              new IsEqual('DEV')
          )
      )
  )
}
