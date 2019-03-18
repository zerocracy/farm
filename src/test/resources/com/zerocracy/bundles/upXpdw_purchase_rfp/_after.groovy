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
package com.zerocracy.bundles.purchase_rfp

import com.jcabi.xml.XML
import com.zerocracy.Farm
import com.zerocracy.Item
import com.zerocracy.Project
import com.zerocracy.pmo.Awards
import com.zerocracy.pmo.Pmo
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers

def exec(Project project, XML xml) {
  Farm farm = binding.variables.farm
  new Pmo(farm).acq('test.txt').withCloseable {
    Item item ->
      MatcherAssert.assertThat(
        'Incorrect candidate message',
        item.path().text,
        Matchers.startsWith('Thanks for purchasing RFP #42; the email of the client is test@zerocracy.com')
      )
  }
  MatcherAssert.assertThat(
    'Didn\'t update reputation',
    new Awards(farm, 'candidate').bootstrap().total(),
    Matchers.equalTo(256)
  )
}
