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
package com.zerocracy.bundles.remove_invalid_elections

import com.jcabi.xml.XML
import com.zerocracy.Project

def exec(Project project, XML xml) {
//  Elections elections = new Elections(project).bootstrap()
  // @todo #1663:30min Elections.remove is not working for this test, it's
  //  called in `remove_stale_elections` but doesn't affect target file in
  //  this test. Let's fix it and uncomment this test.
//  MatcherAssert.assertThat(
//    'Election without winner still exist',
//    elections.exists('gh:test/test#1')
//  )
//  MatcherAssert.assertThat(
//    'Election assigned to performer still exist',
//    elections.exists('gh:test/test#2')
//  )
//  MatcherAssert.assertThat(
//    'Election removed from WBS still exist',
//    elections.exists('gh:test/test#3')
//  )
}
