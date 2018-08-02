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
package com.zerocracy.bundles.dont_refresh_awards_when_no_changes

import com.jcabi.xml.XML
import com.zerocracy.Farm
import com.zerocracy.Project
import com.zerocracy.pmo.Awards

import java.text.SimpleDateFormat

def exec(Project project, XML xml) {
  Farm farm = binding.variables.farm
  Awards awards = new Awards(farm, 'g4s8').bootstrap()
  SimpleDateFormat format = new SimpleDateFormat('yyyy-MM-dd HH:mm:ss.SSS', Locale.US)
  awards.add(project, 15, 'gh:test/test#1', 'test', format.parse('2018-01-31 21:00:00.000'))
  awards.add(project, 100, 'gh:test/test#2', 'test', format.parse('2018-04-30 18:00:00.000'))
  awards.add(project, 10, 'gh:test/test#4', 'test', format.parse('2018-01-31 21:00:00.000'))
}
