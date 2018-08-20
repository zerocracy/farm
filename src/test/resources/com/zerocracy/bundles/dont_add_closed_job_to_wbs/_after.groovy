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
package com.zerocracy.bundles.dont_add_closed_job_to_wbs

import com.jcabi.xml.XML
import com.zerocracy.Project

// @todo #1102:30min 0crat should not add job to WBS and create order if issue
//  is already closed. Correct add_job_to_wbs.groovy so closed jobs aren't added
//  to WBS. Then uncomment this test.
def exec(Project project, XML xml) {
//  MatcherAssert.assertThat(
//    'Closed issue added to WBS',
//    new Wbs(project).bootstrap().exists('gh:test/test#1'),
//    new IsEqual<>(false)
//  )
}
