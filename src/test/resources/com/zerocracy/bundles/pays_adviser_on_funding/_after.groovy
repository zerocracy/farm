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
package com.zerocracy.bundles.pays_adviser_on_funding

import com.jcabi.xml.XML
import com.zerocracy.Project

// @todo #1536:30min Project is not paying adviser when being funded. Correct
//  fund_by_stripe.groovy and donate.groovy to send a payment of 4% of the
//  amount of funding to the project advisor on every funding operation. Then
//  uncomment these tests to make sure that these payments are happening.'
def exec(Project project, XML xml) {
//  Farm farm = binding.variables.farm
//  new Footprint(farm, project).withCloseable {
//    Footprint footprint ->
//    MatcherAssert.assertThat(
//      'Adviser received wrong payment on stripe funding',
//      footprint.collection().find(
//        Filters.and(
//          Filters.eq('project', project.pid()),
//          Filters.eq('type', 'Make payment'),
//          Filters.eq('login', 'yegor256'),
//          Filters.eq('amount', '$4')
//        )
//      ),
//      new IsIterableWithSize<>(
//        new IsEqual<>(1)
//      )
//    )
//    MatcherAssert.assertThat(
//      'Project received wrong funding on stripe funding',
//      footprint.collection().find(
//        Filters.and(
//          Filters.eq('project', project.pid()),
//          Filters.eq('type', 'Notify project'),
//          Filters.eq('project', project.pid()),
//          Filters.eq('amount', '$96')
//        )
//      ),
//      new IsIterableWithSize<>(
//        new IsEqual<>(1)
//      )
//    )
//    MatcherAssert.assertThat(
//      'Adviser received wrong payment on donation',
//      footprint.collection().find(
//        Filters.and(
//          Filters.eq('project', project.pid()),
//          Filters.eq('type', 'Make payment'),
//          Filters.eq('login', 'yegor256'),
//          Filters.eq('amount', '$8')
//        )
//      ),
//      new IsIterableWithSize<>(
//        new IsEqual<>(1)
//      )
//    )
//    MatcherAssert.assertThat(
//      'Project received wrong on donation',
//      footprint.collection().find(
//        Filters.and(
//          Filters.eq('project', project.pid()),
//          Filters.eq('type', 'Notify project'),
//          Filters.eq('project', project.pid()),
//          Filters.eq('amount', '$192')
//        )
//      ),
//      new IsIterableWithSize<>(
//        new IsEqual<>(1)
//      )
//    )
//  }
}
