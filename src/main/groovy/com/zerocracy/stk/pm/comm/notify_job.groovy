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
package com.zerocracy.stk.pm.comm

import com.jcabi.xml.XML
import com.zerocracy.Farm
import com.zerocracy.entry.ClaimsOf
import com.zerocracy.farm.Assume
import com.zerocracy.Project
import com.zerocracy.claims.ClaimIn

// The token must look like: job;gh:zerocracy/farm#123

def exec(Project project, XML xml) {
  new Assume(project, xml).type('Notify job')
  ClaimIn claim = new ClaimIn(xml)
  if (!claim.hasToken()) {
    throw new IllegalArgumentException(
      "Claim of type '${claim.type()}' in ${project.pid()} has no token"
    )
  }
  String[] parts = claim.token().split(';')
  if (parts[0] != 'job') {
    throw new IllegalArgumentException(
      "Something is wrong with this token: ${claim.token()}"
    )
  }
  Farm farm = binding.variables.farm
  String[] slices = parts[1].split(':')
  if (slices[0] == 'gh') {
    String[] coords = slices[1].split('#')
    claim.copy()
      .type('Notify in GitHub')
      .token("github;${coords[0]};${coords[1]}")
      .postTo(new ClaimsOf(farm, project))
  } else if (parts[1] == 'none') {
    claim.copy()
      .type('Notify project')
      .postTo(new ClaimsOf(farm, project))
  } else {
    throw new IllegalStateException(
      String.format(
        'I don\'t know how to notify job "%s"',
        parts[1]
      )
    )
  }
}
