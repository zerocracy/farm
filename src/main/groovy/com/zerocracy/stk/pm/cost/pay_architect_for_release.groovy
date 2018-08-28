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
package com.zerocracy.stk.pm.cost

import com.jcabi.xml.XML
import com.mongodb.client.model.Filters
import com.zerocracy.Farm
import com.zerocracy.Par
import com.zerocracy.Policy
import com.zerocracy.Project
import com.zerocracy.claims.ClaimIn
import com.zerocracy.claims.Footprint
import com.zerocracy.entry.ClaimsOf
import com.zerocracy.farm.Assume
import com.zerocracy.pm.staff.Roles
import com.zerocracy.pm.time.Releases
import java.time.Duration
import java.time.Instant

/**
 * Every time a new release comes out, architect gets a bonus equal
 * to the amount of footprint items" since the last release.
 * Routine items like `Ping` will be excluded.
 * The bonus has a limit, specified in policy.
 *
 * @param project Current project
 * @param xml Claim
 */
def exec(Project project, XML xml) {
  new Assume(project, xml).notPmo()
  new Assume(project, xml).type('Release was published')
  Farm farm = binding.variables.farm
  ClaimIn claim = new ClaimIn(xml)
  Releases releases = new Releases(project).bootstrap()
  Instant latest = releases.latest()
  releases.add(claim.param('repo'), claim.param('tag'), Instant.parse(claim.param('date')))
  Policy policy = new Policy(farm)
  Duration mpc = Duration.ofMinutes(policy.get('54.min-per-claim', 2))
  Duration max = Duration.ofHours(policy.get('54.max', 4))
  long claims = new Footprint(farm, project).withCloseable { Footprint footprint ->
    footprint.collection().countDocuments(
      Filters.and(
        Filters.gt('created', Date.from(latest)),
        Filters.or(
          Filters.eq('type', 'Order was given'),
          Filters.eq('type', 'Order was canceled'),
          Filters.eq('type', 'Order was finished'),
          Filters.eq('type', 'Request order start'),
          Filters.eq('type', 'Job removed from WBS'),
          Filters.eq('type', 'Job was added to WBS'),
          Filters.eq('type', 'Quality review completed'),
          Filters.eq('type', 'Register impediment'),
          Filters.eq('type', 'Set boost'),
          Filters.eq('type', 'Assign role'),
          Filters.eq('type', 'Resign role'),
          Filters.eq('type', 'Job was declined'),
        ),
      )
    )
  }
  int minutes = (int) Math.min(claims * mpc.toMinutes(), max.toMinutes())
  List<String> arcs = new Roles(project).bootstrap().findByRole('ARC')
  int mpa = (int) (minutes / arcs.size())
  arcs.each {
    claim.copy()
      .type('Make payment')
      .param('login', it)
      .param('reason', new Par('Release bonus for ARC §54').say())
      .param('minutes', mpa)
      .param('job', 'none')
      .postTo(new ClaimsOf(farm, project))
    claim.copy()
      .type('Notify PMO')
      .param(
      'message',
      new Par('We just sent "ARC release bonus" of %d minutes to %s in %s (%d claims)')
        .say(mpa, it, project.pid(), claims)
    ).postTo(new ClaimsOf(farm))
  }
}
