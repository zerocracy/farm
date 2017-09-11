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
package com.zerocracy.farm.ruled;

import com.jcabi.aspects.Tv;
import com.zerocracy.jstk.Project;
import com.zerocracy.jstk.fake.FkProject;
import com.zerocracy.pm.cost.Boosts;
import com.zerocracy.pm.scope.Wbs;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link RdItem}.
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.17
 * @checkstyle JavadocMethodCheck (500 lines)
 */
public final class RdItemTest {

    @Test
    public void closesClaims() throws Exception {
        final Project project = new RdProject(new FkProject());
        final String first = "gh:test/test#1";
        new Wbs(project).bootstrap().add(first);
        final String second = "gh:test/test#2";
        new Wbs(project).bootstrap().add(second);
        new Boosts(project).bootstrap().boost(first, Tv.TEN);
        new Boosts(project).bootstrap().boost(second, Tv.TEN);
        new Wbs(project).remove(first);
        MatcherAssert.assertThat(
            new Boosts(project).factor(first),
            Matchers.not(Matchers.equalTo(Tv.TEN))
        );
        MatcherAssert.assertThat(
            new Boosts(project).factor(second),
            Matchers.equalTo(Tv.TEN)
        );
    }

}
