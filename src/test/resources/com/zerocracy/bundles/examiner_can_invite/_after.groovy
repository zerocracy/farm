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
package com.zerocracy.bundles.examiner_can_invite

import com.jcabi.xml.XML
import com.zerocracy.Farm
import com.zerocracy.Par
import com.zerocracy.Project
import com.zerocracy.pmo.Awards
import com.zerocracy.pmo.People
import org.hamcrest.MatcherAssert
import org.hamcrest.core.IsEqual
import org.hamcrest.core.StringContains

def exec(Project project, XML xml) {
  Farm farm = binding.variables.farm
  People people = new People(farm).bootstrap()
  String friend = 'friend'
  MatcherAssert.assertThat(
    people.hasMentor(friend),
    new IsEqual<>(true)
  )
  String user = 'user'
  MatcherAssert.assertThat(
    people.mentor(friend),
    new IsEqual<>(user)
  )
  MatcherAssert.assertThat(
    new Awards(farm, user).bootstrap().total(),
    new IsEqual<>(1138)
  )
  project.acq('test.txt').withCloseable {
    item -> MatcherAssert.assertThat(
      item.path().text,
      new StringContains(
        new Par('You received bonus %d points for @%s resume examination').say(32, friend)
      )
    )
  }
}
