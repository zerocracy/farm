/**
 * Copyright (c) 2016-2017 Zerocracy
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
package com.zerocracy.bundles.bug_label

import com.jcabi.xml.XML
import com.zerocracy.jstk.Project
import com.zerocracy.pm.scope.Wbs

def exec(Project project, XML xml) {
// @todo #69:15min This assertion is disabled because the mock Github pull
//  request created in _before.groovy is not recognized as a valid pull request.
//  See https://github.com/jcabi/jcabi-github/issues/1323. When the issue is
//  fixed, jcabi-github version should be updated and the assertion should be
//  enabled.
// assert new Wbs(project).bootstrap().exists('gh:test/test#1')
}
