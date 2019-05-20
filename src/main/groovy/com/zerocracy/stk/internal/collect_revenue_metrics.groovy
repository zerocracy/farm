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

import com.jcabi.log.Logger
import com.jcabi.xml.XML
import com.zerocracy.Farm
import com.zerocracy.Project
import com.zerocracy.cash.Cash
import com.zerocracy.claims.ClaimIn
import com.zerocracy.db.ExtDataSource
import com.zerocracy.farm.Assume
import com.zerocracy.farm.props.Props
import com.zerocracy.kpi.KpiOf
import com.zerocracy.kpi.Metric
import com.zerocracy.pm.cost.PgLedger

import java.time.Duration
import java.time.Instant

/**
 * Collect KPI for project fees.
 *
 * @param project Project
 * @param xml Claim
 */
def exec(Project pmo, XML xml) {
  new Assume(pmo, xml).type('Ping daily').isPmo()
  Farm farm = binding.variables.farm
  if (new Props(farm).has('//testing')) {
    Logger.info('collect_revenue_metrics', 'Skipping in testing mode')
    return
  }
  ClaimIn claim = new ClaimIn(xml)
  Instant since = claim.created().toInstant() - Duration.ofDays(30)
  PgLedger ledger = new PgLedger(new ExtDataSource(farm).value(), pmo)
  Cash revenue = ledger.fees(since)
  new Metric.S('revenue_monthly', revenue.decimal().doubleValue()).send(new KpiOf(farm))
}
