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
import com.zerocracy.farm.Assume
import com.zerocracy.kpi.KpiMetrics
import com.zerocracy.kpi.KpiOf
import com.zerocracy.kpi.Metric
import com.zerocracy.pmo.Catalog
import com.zerocracy.pmo.People
import org.cactoos.list.ListOf

/**
 * Collect KPI metrics.
 *
 * @param project Project
 * @param xml Claim
 */
def exec(Project pmo, XML xml) {
  new Assume(pmo, xml).type('Ping daily').isPmo()
  Farm farm = binding.variables.farm
  KpiMetrics kpi = new KpiOf(farm)
  Catalog catalog = new Catalog(farm).bootstrap()
  People people = new People(farm).bootstrap()
  new ListOf<Metric>(
    new Metric.S(
      'active_projects', catalog.active().size()
    ),
    new Metric.S(
      'active_users', people.active().size()
    ),
    new Metric.S(
      'visible_users', people.visible().size()
    ),
    new Metric.S(
      'total_reputation', people.totalReputation()
    )
  ).each { metric -> metric.send(kpi) }
}
