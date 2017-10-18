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
package com.zerocracy.stk.pm.time.reminders

import com.jcabi.log.Logger
import com.jcabi.xml.XML
import com.zerocracy.farm.Assume
import com.zerocracy.jstk.Project
import com.zerocracy.pm.ClaimIn
import com.zerocracy.pm.in.Orders
import com.zerocracy.pm.time.Reminders
import java.time.ZoneOffset
import java.time.ZonedDateTime
import org.cactoos.collection.Mapped
import org.cactoos.iterable.LengthOf

/**
 * @todo #266:30min We need to schedule this stakeholder which
 *  will collect jobs to remind. It may be periodic task or it
 *  can be triggered by some event. Also we need to notify users
 *  in github with reminder's label.
 */
def exec(Project project, XML xml) {
  new Assume(project, xml).type('Collect reminder')

  def claim = new ClaimIn(xml)
  def reminders = new Reminders(project).bootstrap()
  def orders = new Orders(project).bootstrap()
  def processed = new LengthOf(
    new Mapped<>(
      orders.olderThan(
        ZonedDateTime.ofInstant(
          claim.created().toInstant(), ZoneOffset.UTC
        ).minusDays(5)
      ),
      { String job ->
        reminders.add(job, orders.performer(job), '5 days')
        job
      }
    )
  ).value()
  Logger.info('remind.groovy', "processed $processed orders")
}
