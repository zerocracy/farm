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
package com.zerocracy.bundles.terminates_orders_wbs_on_link_removal

import com.jcabi.xml.XML
import com.zerocracy.Project
import com.zerocracy.pm.in.Orders
import com.zerocracy.pm.scope.Wbs
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers

def exec(Project project, XML xml) {
  Orders orders = new Orders(project).bootstrap()
  MatcherAssert.assertThat(orders.jobs('cmiranda'), Matchers.empty())
  MatcherAssert.assertThat(orders.jobs('krzyk'), Matchers.empty())
  MatcherAssert.assertThat(orders.jobs('yegor256'), Matchers.empty())
  MatcherAssert.assertThat(
    new Wbs(project).bootstrap().iterate(),
    Matchers.empty()
  )
}
