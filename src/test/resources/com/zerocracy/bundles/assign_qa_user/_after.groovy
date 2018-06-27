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
package com.zerocracy.bundles.assign_qa_user

import com.jcabi.matchers.XhtmlMatchers
import com.jcabi.xml.XML
import com.zerocracy.Farm
import com.zerocracy.Item
import com.zerocracy.Project
import com.zerocracy.Xocument
import com.zerocracy.cash.Cash
import com.zerocracy.pmo.Agenda
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers

def exec(Project project, XML xml) {
  Farm farm = binding.variables.farm
  String job = 'gh:test/test#1'
  MatcherAssert.assertThat(
    new Agenda(farm, 'yegor256').bootstrap().hasInspector(job),
    Matchers.is(true)
  )
  project.acq('reviews.xml').withCloseable { Item item ->
    MatcherAssert.assertThat(
      new Xocument(item.path()),
      XhtmlMatchers.hasXPath(
        "/reviews/review[@job = '${job}']/bonus[text() = '${new Cash.S('$8')}']"
      )
    )
  }
}
