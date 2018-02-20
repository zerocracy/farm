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
package com.zerocracy.bundles.jobs_to_remind

import com.jcabi.xml.XML
import com.zerocracy.Project
import com.zerocracy.pm.in.Orders
import com.zerocracy.pm.time.Reminders
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers

def exec(Project project, XML xml) {
  Reminders reminders = new Reminders(project).bootstrap()
  MatcherAssert.assertThat(
    'order #1 shouldn\'t be added to reminders',
    reminders.labels('gh:test/test#1'),
    Matchers.emptyIterable()
  )
  MatcherAssert.assertThat(
    'order #2 with 5-days label was not found in reminders',
    reminders.labels('gh:test/test#2'),
    Matchers.allOf(
      Matchers.iterableWithSize(1),
      Matchers.contains('5 days')
    )
  )
  MatcherAssert.assertThat(
    'order #3 with 8-days label was not found in reminders',
    reminders.labels('gh:test/test#3'),
    Matchers.allOf(
      Matchers.iterableWithSize(1),
      Matchers.contains('8 days')
    )
  )
  Orders orders = new Orders(project).bootstrap()
  MatcherAssert.assertThat(
    'order #4 was not unassigned',
    orders.assigned('gh:test/test#4'),
    Matchers.is(false)
  )
}
