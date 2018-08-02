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
import com.zerocracy.Project
import com.zerocracy.entry.ClaimsOf
import com.zerocracy.farm.Assume
import com.zerocracy.claims.ClaimIn
import com.zerocracy.pmo.Catalog
import com.zerocracy.pmo.People
import com.zerocracy.pmo.Projects

// The token must look like: yegor256

def exec(Project project, XML xml) {
  new Assume(project, xml).type('Notify user')
  ClaimIn claim = new ClaimIn(xml)
  String[] parts = claim.token().split(';')
  if (parts[0] != 'user') {
    throw new IllegalArgumentException(
      "Something is wrong with this token: ${claim.token()}"
    )
  }
  String login = parts[1]
  Farm farm = binding.variables.farm
  People people = new People(farm).bootstrap()
  Catalog catalog = new Catalog(farm).bootstrap()
  if (!catalog.exists(project.pid())) {
    throw new IllegalStateException(
      "Project ${project.pid()} doesn't exist in the catalog, can't notify user"
    )
  }
  Boolean done = false
  people.links(login, 'telegram').any { uid ->
    claim.copy()
      .type('Notify in Telegram')
      .token("telegram;${uid}")
      .param('login', login)
      .postTo(new ClaimsOf(farm, project))
    done = true
    done
  }
  new Projects(farm, login).bootstrap().iterate().any { pid ->
    catalog.links(pid, 'slack').any { channel ->
      people.links(login, 'slack').any { sid ->
        claim.copy()
          .type('Notify in Slack')
          .token("slack;${channel};${sid};direct")
          .param('login', login)
          .postTo(new ClaimsOf(farm, project))
        done = true
        done
      }
      done
    }
    done
  }
}
