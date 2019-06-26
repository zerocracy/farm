/*
 * Copyright (c) 2016-2019 Zerocracy
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
import com.zerocracy.Txn
import com.zerocracy.claims.ClaimIn
import com.zerocracy.entry.ClaimsOf
import com.zerocracy.farm.Assume
import com.zerocracy.farm.props.Props
import com.zerocracy.kpi.KpiMetrics
import com.zerocracy.kpi.KpiOf
import com.zerocracy.kpi.KpiStats
import com.zerocracy.pm.cost.Ledger
import com.zerocracy.pmo.Catalog

import java.time.Duration
import java.time.Instant

/**
 * Send KPI metrics to PMO users.
 *
 * @param pmo Project
 * @param xml Claim
 */
def exec(Project pmo, XML xml) {
  new Assume(pmo, xml).type('Ping daily').isPmo()
  Farm farm = binding.variables.farm
  if (new Props(farm).has('//testing')) {
    return
  }
  KpiMetrics metrics = new KpiOf(farm)
  int days = 1
  Duration period = Duration.ofDays(days)
  StringBuilder builder = new StringBuilder()
  builder.append("Hey, the stats for the last ${days} days:\n\n")
  metrics.metrics().each { name ->
    KpiStats stats = metrics.statistic(name, period)
    if (stats.count() > 0) {
      double avg = Math.round(stats.avg())
      double min = Math.round(stats.min())
      double max = Math.round(stats.max())
      builder.append("  `${name}`:")
      if (avg != min || avg != max) {
        builder.append(" ${avg}/${min}/${max}")
      } else {
        builder.append(" ${avg}")
      }
      if (stats.count() > 1) {
        builder.append(" ${stats.count()}e")
      }
      builder.append('\n')
    }
  }
  new Txn(pmo).withCloseable { Project pkt ->
    Instant start = Instant.now() - Duration.ofDays(16)
    builder.append('Active projects:')
    Catalog catalog = new Catalog(pkt).bootstrap()
    catalog.active().each { String pid ->
      Ledger ledger = new Ledger(farm, farm.find("@id='${pid}'").iterator().next()).bootstrap()
      if (!ledger.empty(start)) {
        String arc = catalog.architect(pid)
        String title = catalog.title(pid)
        builder.append("  `${pid}`/`${title}`/`${arc}`\n")
      }
    }
    builder.append('\n')
  }
  builder.append('\nIf you have any questions,')
    .append(' don\'t hesitate to check the [dashboard](https://www.0crat.com).')
  new ClaimIn(xml).copy()
    .type('Notify PMO')
    .param('message', builder.toString())
    .postTo(new ClaimsOf(farm))
}
