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
package com.zerocracy.pm.in;

import com.zerocracy.Project;
import com.zerocracy.SoftException;
import com.zerocracy.farm.fake.FkProject;
import com.zerocracy.farm.props.PropsFarm;
import com.zerocracy.pm.scope.Wbs;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link Impediments}.
 *
 * @since 1.0
 *  @checkstyle JavadocMethodCheck (500 lines)
 */
public final class ImpedimentsTest {

    @Test
    public void registerImpediment() throws Exception {
        final Project project = new FkProject();
        final Impediments imp = new Impediments(new PropsFarm(), project)
            .bootstrap();
        final String job = "gh:test/test#1";
        new Wbs(project).bootstrap().add(job);
        new Orders(new PropsFarm(), project).bootstrap()
            .assign(job, "yegor256", 0L);
        imp.register(job, "test");
        MatcherAssert.assertThat(
            imp.jobs(),
            Matchers.contains(job)
        );
        MatcherAssert.assertThat(
            imp.exists(job),
            Matchers.is(true)
        );
    }

    @Test
    public void removesImpediment() throws Exception {
        final Project project = new FkProject();
        final PropsFarm farm = new PropsFarm();
        final Impediments imp = new Impediments(farm, project).bootstrap();
        final String job = "gh:test/test#2";
        new Wbs(project).bootstrap().add(job);
        new Orders(farm, project).bootstrap().assign(job, "amihaiemil", 0L);
        imp.register(job, "reason");
        MatcherAssert.assertThat(
            imp.jobs(),
            Matchers.contains(job)
        );
        MatcherAssert.assertThat(
            imp.exists(job),
            Matchers.is(true)
        );
        imp.remove(job);
        MatcherAssert.assertThat(
            imp.jobs(),
            Matchers.not(Matchers.contains(job))
        );
        MatcherAssert.assertThat(
            imp.exists(job),
            Matchers.is(false)
        );
    }

    @Test(expected = SoftException.class)
    public void removesMissingImpediment() throws Exception {
        final Impediments imp =
            new Impediments(new PropsFarm(), new FkProject()).bootstrap();
        imp.remove("gh:test/test#8");
    }
}
