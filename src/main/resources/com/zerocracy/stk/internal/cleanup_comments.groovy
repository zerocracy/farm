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
package com.zerocracy.stk.internal

import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator
import com.amazonaws.services.dynamodbv2.model.Condition
import com.jcabi.dynamo.QueryValve
import com.jcabi.github.Coordinates
import com.jcabi.log.Logger
import com.jcabi.xml.XML
import com.zerocracy.entry.ExtDynamo
import com.zerocracy.entry.ExtGithub
import com.zerocracy.farm.Assume
import com.zerocracy.farm.props.Props
import com.zerocracy.jstk.Farm
import com.zerocracy.jstk.Project
import java.util.concurrent.TimeUnit

def exec(Project project, XML xml) {
  new Assume(project, xml).notPmo()
  new Assume(project, xml).type('Ping')
  Farm farm = binding.variables.farm
  def github = new ExtGithub(farm).value()
  if (new Props(farm).has('//testing')) {
    Logger.info(this, 'skip in testing mode')
    return
  }
  new ExtDynamo(farm).value().table('0crat-errors').frame()
    .through(new QueryValve().withLimit(10))
    .where(
    'created',
    new Condition()
      .withComparisonOperator(ComparisonOperator.LT)
      .withAttributeValueList(
      new AttributeValue()
        .withN(Long.toString(new Date().time - TimeUnit.HOURS.toMillis(72L)))
    )
  ).iterator().each { item ->
    def issue = item.get('issue').s
    def comment = item.get('comment').n
    github
      .repos()
      .get(new Coordinates.Simple(issue.split('#')[0]))
      .issues()
      .get(Integer.valueOf(issue.split('#')[1]))
      .comments()
      .get(Integer.valueOf(comment))
      .remove()
  }
}