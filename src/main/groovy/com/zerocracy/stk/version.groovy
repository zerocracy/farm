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
package com.zerocracy.stk

import com.jcabi.xml.XML
import com.zerocracy.Par
import com.zerocracy.entry.ClaimsOf
import com.zerocracy.farm.Assume
import com.zerocracy.farm.props.Props
import com.zerocracy.Farm
import com.zerocracy.Project
import com.zerocracy.claims.ClaimIn

def exec(Project project, XML xml) {
  new Assume(project, xml).type('Version')
  Farm farm = binding.variables.farm
  Props props = new Props(farm)
  new ClaimIn(xml).reply(
    new Par(
      "My version is `%s`, rev.`%s`, built on %s"
    ).say(props.get('//build/version', ''),
      props.get('//build/revision', ''),
      props.get('//build/date', ''))
  ).postTo(new ClaimsOf(farm, project))
}
