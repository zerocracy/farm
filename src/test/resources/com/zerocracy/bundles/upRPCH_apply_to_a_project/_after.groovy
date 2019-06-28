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
package com.zerocracy.bundles.apply_to_a_project

import com.jcabi.matchers.XhtmlMatchers
import com.jcabi.xml.XML
import com.zerocracy.Farm
import com.zerocracy.Item
import com.zerocracy.Project
import com.zerocracy.pm.staff.Applications
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers

def exec(Project pmo, XML claim) {
  pmo.acq('test.txt').withCloseable { Item item ->
    MatcherAssert.assertThat(
      'user did\'t receive a message',
      item.path().toFile().readLines(),
      Matchers.hasItem(Matchers.stringContainsInOrder(['The project', 'was notified about your desire to join them']))
    )
  }
  Farm farm = binding.variables.farm
  Project project = farm.find('@id="XXXXXX000"').first()
  MatcherAssert.assertThat(
    'application wan\'t added to list',
    new Applications(project).bootstrap().all(),
    Matchers.contains(
      XhtmlMatchers.hasXPaths(
        '/application[@login="g4s8"]',
        '/application/rate[text()="$42.00"]',
        '/application/role[text()="DEV"]'
      )
    )
  )
}
