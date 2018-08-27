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
package com.zerocracy.bundles.send_correct_zold_value

import com.jcabi.xml.XML
import com.mongodb.client.model.Filters
import com.zerocracy.Farm
import com.zerocracy.Project
import com.zerocracy.claims.Footprint
import org.hamcrest.MatcherAssert
import org.hamcrest.collection.IsIterableWithSize
import org.hamcrest.core.IsEqual

def exec(Project project, XML xml) {
  Farm farm = binding.variables.farm
  new Footprint(farm, project).withCloseable { Footprint footprint ->
    MatcherAssert.assertThat(
      'User with vesting received wrong ZLD value',
      footprint.collection().find(
        Filters.and(
          Filters.eq('type', 'Notify user'),
          Filters.eq('login', 'krzyk'),
          Filters.eq('message', 'We just sent you 32 ZLD through https://wts.zold.io'),
        )
      ),
      new IsIterableWithSize<>(
          new IsEqual<>(1)
      )
    )
    MatcherAssert.assertThat(
      'User without vesting received wrong ZLD value',
      footprint.collection().find(
        Filters.and(
          Filters.eq('type', 'Notify user'),
          Filters.eq('login', 'amihaiemil'),
          Filters.eq('message', 'We just sent you 16 ZLD through https://wts.zold.io'),
        )
      ),
      new IsIterableWithSize<>(
          new IsEqual<>(1)
      )
    )
  }
}
