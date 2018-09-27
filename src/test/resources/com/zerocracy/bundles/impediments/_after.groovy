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
package com.zerocracy.bundles.impediments

import com.jcabi.xml.XML
import com.zerocracy.Project
import com.zerocracy.pm.in.Impediments
import com.zerocracy.pm.in.Orders
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers

/**
 * @todo #447:30min register_impediment stakeholder should pay attention
 *  to impediment count requested by user and not allow to register new
 *  impediments if threshold has been reached. See #447 comments for details.
 */
def exec(Project project, XML xml) {
    Orders orders = new Orders(farm, project).bootstrap()
    def impediments = new Impediments(farm, project).bootstrap().jobs()
    MatcherAssert.assertThat(
        'impediment was not registered for job #1',
        impediments,
        Matchers.hasItem('gh:test/test#1')
    )
//  MatcherAssert.assertThat(
//    'impediment was registered for job #2',
//    impediments,
//    Matchers.not(Matchers.hasItem('gh:test/test#2'))
//  )
    MatcherAssert.assertThat(
        'order #1 was unassigned',
        orders.assigned('gh:test/test#1'),
        Matchers.is(true)
    )
}
