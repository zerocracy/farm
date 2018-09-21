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
package com.zerocracy.stk.internal

import com.jcabi.xml.XML
import com.zerocracy.Farm
import com.zerocracy.Project
import com.zerocracy.claims.ClaimIn
import com.zerocracy.entry.ClaimsOf
import com.zerocracy.farm.Assume
import com.zerocracy.kpi.KpiMetrics
import com.zerocracy.kpi.KpiOf
import com.zerocracy.kpi.KpiStats

import java.time.Duration
import java.time.Instant

/**
 * Send KPI metrics to PMO users.
 *
 * @param project Project
 * @param xml Claim
 */
def exec(Project project, XML xml) {
  new Assume(project, xml).type('Ping daily')
  new Assume(project, xml).isPmo()
  Farm farm = binding.variables.farm
  KpiMetrics metrics = new KpiOf(farm)
  Duration period = Duration.ofDays(1)
  StringBuilder builder = new StringBuilder()
  builder.append("KPI for period: `${(Instant.now() - period)}` - `${Instant.now()}`\n")
  metrics.metrics().each { name ->
    KpiStats stats = metrics.statistic(name, period)
    if (stats.count() > 0) {
      builder.append("`${name}`:\t")
      if (!isCloseTo(stats.min(), 1) || !isCloseTo(stats.max(), 1)) {
        builder.append("avg=`${stats.avg()}` (min=`${stats.min()}` - max=`${stats.max()}`) ")
      }
      builder.append("`${stats.count()}` events\n")
    }
  }
  new ClaimIn(xml).copy()
    .type('Notify PMO')
    .param('message', builder.toString())
    .postTo(new ClaimsOf(farm))
}

boolean isCloseTo(double value, double target) {
  Math.abs(value - target) - 0.0001 <= 0.0
}
