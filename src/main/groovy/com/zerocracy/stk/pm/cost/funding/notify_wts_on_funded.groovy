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
package com.zerocracy.stk.pm.cost.funding

import com.jcabi.http.request.JdkRequest
import com.jcabi.xml.XML
import com.zerocracy.Farm
import com.zerocracy.Project
import com.zerocracy.claims.ClaimIn
import com.zerocracy.farm.Assume
import com.zerocracy.farm.props.Props

/**
 * This stakeholder is called when project is funded by Stripe,
 * it sends a callback to WTS when any project funded by stripe.
 *
 * @param project Funded project
 * @param xml Claim
 *
 */
def exec(Project project, XML xml) {
  new Assume(project, xml).notPmo().type('Funded by Stripe')
  Farm farm = binding.variables.farm
  Props props = new Props(farm)
  if (props.has('//testing')) {
    return
  }
  String host = props.get('//zold/host')
  ClaimIn claim = new ClaimIn(xml)
  String amount = claim.param('amount')
  new JdkRequest("https://www.rehttp.net/p/${host}/funded")
    .uri()
    .queryParam('amount', amount)
    .back()
    .header('X-Zold-Wts', props.get('//zold/secret'))
    .fetch()
}
