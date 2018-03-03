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
package com.zerocracy.bundles.close_the_job

import com.jcabi.xml.XML
import com.zerocracy.Farm
import com.zerocracy.Project
import com.zerocracy.pm.scope.Wbs
import com.zerocracy.pmo.Awards
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers

/**
 * @todo #539:30min The ticket should be removed from WBS and performer
 *  should receive payment and awards only if ticket was closed by the reporter,
 *  ARC or PO. In other cases we should say "sorry, we won't charge".
 *  So let's provide who close an issue in claim params in `RbOnClose`
 *  and check it in 'Close job' and 'Remove job from WBS' scripts, then
 *  commented tests will be worked. Also policy have to be updated.
 */
@SuppressWarnings('UnnecessaryObjectReferences')
def exec(Project project, XML xml) {
  Farm farm = binding.variables.farm
  Wbs wbs = new Wbs(project).bootstrap()
  MatcherAssert.assertThat(
    'Issue requested to close by author still in WBS',
    wbs.iterate(),
    Matchers.not(Matchers.contains(Matchers.equalTo('gh:test/test#1')))
  )
  MatcherAssert.assertThat(
    'Assignee didn\'t receive awards for issue closed by author',
    new Awards(farm, 'dev1').bootstrap().total(),
    Matchers.greaterThan(0)
  )
//  MatcherAssert.assertThat(
//    'Issue requested to close by rultor is not in WBS',
//    wbs.iterate(),
//    Matchers.contains(Matchers.equalTo('gh:test/test#2'))
//  )
//  MatcherAssert.assertThat(
//    'Assignee received awards for issue closed by rultor',
//    new Awards(farm, 'dev2').bootstrap().total(),
//    Matchers.equalTo(0)
//  )
  MatcherAssert.assertThat(
    'Issue requested to close by ARC still in WBS',
    wbs.iterate(),
    Matchers.not(Matchers.contains(Matchers.equalTo('gh:test/test#3')))
  )
  MatcherAssert.assertThat(
    'Assignee didn\'t receive awards for issue closed by ARC',
    new Awards(farm, 'dev3').bootstrap().total(),
    Matchers.greaterThan(0)
  )
  MatcherAssert.assertThat(
    'Issue requested to close by PO still in WBS',
    wbs.iterate(),
    Matchers.not(Matchers.contains(Matchers.equalTo('gh:test/test#4')))
  )
  MatcherAssert.assertThat(
    'Assignee didn\'t receive awards for issue closed by PO',
    new Awards(farm, 'dev4').bootstrap().total(),
    Matchers.greaterThan(0)
  )
}
