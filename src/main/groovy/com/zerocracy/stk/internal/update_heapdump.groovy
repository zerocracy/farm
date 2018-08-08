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
package com.zerocracy.stk.internal

import com.jcabi.log.Logger
import com.jcabi.s3.Bucket
import com.jcabi.s3.fake.FkBucket
import com.jcabi.xml.XML
import com.zerocracy.Farm
import com.zerocracy.Project
import com.zerocracy.entry.ExtBucket
import com.zerocracy.entry.HeapDump
import com.zerocracy.farm.Assume
import com.zerocracy.farm.props.Props

def exec(Project project, XML xml) {
  new Assume(project, xml).isPmo()
  new Assume(project, xml).type('Ping hourly')
  Farm farm = binding.variables.farm
  if (new Props(farm).has('//testing')) {
    project.acq('test').withCloseable {
      if (it.path().toString().contains('update_heapdump')) {
        Bucket bucket
        project.acq('test/bucket').withCloseable {
          bucket = new FkBucket(
              it.path(),
              'dumpbucket'
          )
        }
        Logger.info(this, 'Saving test heap')
        new HeapDump(
            bucket,
            '',
            it.path(),
            'heap'
        ).save()
      }
    }
  } else {
    try {
      new HeapDump(new ExtBucket(farm).value(), '').save()
    } catch (IOException err) {
      Logger.info(this, "Heap dump doesn't exist: ${err.message}")
    }
  }
}
