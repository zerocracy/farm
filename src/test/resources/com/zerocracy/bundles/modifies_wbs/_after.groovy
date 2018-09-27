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
package com.zerocracy.bundles.modifies_wbs

import com.jcabi.xml.XML
import com.zerocracy.Project
import com.zerocracy.pm.in.Orders
import com.zerocracy.pm.scope.Wbs
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers

def exec(Project project, XML xml) {
  Wbs wbs = new Wbs(project).bootstrap()
  assert wbs.exists('gh:test/test#1')
  def orders = new Orders(farm, project).bootstrap()
  MatcherAssert.assertThat(
    orders.assigned('gh:test/test#3'),
    Matchers.is(true)
  )
  MatcherAssert.assertThat(
    orders.performer('gh:test/test#3'),
    Matchers.equalTo('g4s8')
  )
}
