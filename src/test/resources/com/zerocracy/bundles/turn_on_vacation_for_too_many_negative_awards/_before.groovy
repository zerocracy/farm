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
package com.zerocracy.bundles.refresh_awards

import com.jcabi.github.Repos
import com.jcabi.xml.XML
import com.zerocracy.Farm
import com.zerocracy.Project
import com.zerocracy.entry.ExtGithub
import com.zerocracy.pmo.Awards

@SuppressWarnings('UnnecessaryObjectReferences')
def exec(Project project, XML xml) {
  Farm farm = binding.variables.farm
  Awards awards = new Awards(farm, 'carlosmiranda').bootstrap()
  new ExtGithub(farm).value().repos()
    .create(new Repos.RepoCreate('test', false))
  awards.add(project, -1, 'gh:test/test#1', 'test', new Date(1517432400000L))
  awards.add(project, -2, 'gh:test/test#1', 'test', new Date(1517432400001L))
  awards.add(project, -3, 'gh:test/test#1', 'test', new Date(1517432400002L))
  awards.add(project, -4, 'gh:test/test#1', 'test', new Date(1517432400003L))
  awards.add(project, -5, 'gh:test/test#1', 'test', new Date(1525122000004L))
  awards.add(project, -6, 'gh:test/test#1', 'test', new Date(1517432400005L))
  awards.add(project, -7, 'gh:test/test#1', 'test', new Date(1517432400006L))
  awards.add(project, -8, 'gh:test/test#1', 'test', new Date(1525122000007L))
}
