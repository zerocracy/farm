/**
 * Copyright (c) 2016-2017 Zerocracy
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

import com.google.common.collect.Lists
import com.jcabi.aspects.Tv
import com.jcabi.github.Bulk
import com.jcabi.github.Comment
import com.jcabi.github.Coordinates
import com.jcabi.github.Issue
import com.jcabi.github.Repo
import com.jcabi.github.Smarts
import com.jcabi.xml.XML
import com.zerocracy.farm.Assume
import com.zerocracy.jstk.Project
import com.zerocracy.pm.ClaimIn
import com.zerocracy.pmo.ext.ExtGithub
import com.zerocracy.radars.github.GhTube

def exec(Project project, XML xml) {
  new Assume(project, xml).type('Notify in GitHub')
  ClaimIn claim = new ClaimIn(xml)
  String[] parts = claim.token().split(';')
  Repo repo = new ExtGithub(project).asValue().repos().get(
    new Coordinates.Simple(parts[1])
  )
  Issue issue = safe(
    repo.issues().get(
      Integer.parseInt(parts[2])
    )
  )
  String message = claim.param('message')
  if (parts.length > Tv.THREE) {
    Comment comment = issue.comments().get(
      Integer.parseInt(parts[Tv.THREE])
    )
    new GhTube(comment).say(message)
  } else {
    issue.comments().post(message)
  }
}

static Issue safe(Issue issue) {
  List<Comment.Smart> comments = Lists.newArrayList(
    new Bulk<>(
      new Smarts<>(
        issue.comments().iterate(new Date(0L))
      )
    )
  )
  Collections.reverse(comments)
  if (over(issue, comments)) {
    throw new IllegalStateException(
      String.format(
        'Can\'t post anything to %s#%d, too many comments already',
        issue.repo().coordinates(), issue.number()
      )
    )
  }
  issue
}

static boolean over(Issue issue, List<Comment.Smart> list) {
  String self = issue.repo().github().users().self().login()
  boolean over = false
  for (int idx = 0; idx < list.size(); ++idx) {
    if (idx >= Tv.FIVE) {
      over = true
      break
    }
    if (!list[idx].author().login().equalsIgnoreCase(self)) {
      break
    }
  }
  over
}
