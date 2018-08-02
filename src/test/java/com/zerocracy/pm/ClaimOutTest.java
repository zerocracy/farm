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
package com.zerocracy.pm;

import com.jcabi.matchers.XhtmlMatchers;
import com.zerocracy.Project;
import com.zerocracy.claims.ClaimOut;
import com.zerocracy.claims.Claims;
import com.zerocracy.claims.ClaimsItem;
import com.zerocracy.entry.ClaimsOf;
import com.zerocracy.farm.fake.FkProject;
import com.zerocracy.farm.props.PropsFarm;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.takes.misc.Concat;
import org.xembly.Directive;

/**
 * Test case for {@link ClaimOut}.
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class ClaimOutTest {

    @Test
    public void chainsThem() throws Exception {
        final ClaimsItem claims = new ClaimsItem(new FkProject()).bootstrap();
        claims.add(
            new Concat<Directive>(
                new ClaimOut()
                    .type("Notify")
                    .token("test;token")
                    .param("message", "hello, world"),
                new ClaimOut()
                    .type("Hello")
            )
        );
        MatcherAssert.assertThat(
            claims.iterate(),
            Matchers.iterableWithSize(2)
        );
    }

    @Test
    public void postProcessesClaim() throws Exception {
        final Project project = new FkProject();
        new ClaimOut()
            .type("Notify")
            .token("test;token")
            .param("login", "@yegor256")
            .param("job", "gh:zerocracy/zerocracy.github.io#3")
            .param("minutes", "45min")
            .param("cause_type", "Ping")
            .param("message", "hello, world")
            .postTo(new ClaimsOf(new PropsFarm(), project));
        MatcherAssert.assertThat(
            XhtmlMatchers.xhtml(
                new ClaimsItem(project).iterate().iterator().next()
            ),
            XhtmlMatchers.hasXPaths(
                "/claim/params/param[@name='login' and .='yegor256']",
                "/claim/params/param[@name='minutes' and .='45']"
            )
        );
    }

    @Test
    public void complainsAboutInvalidClaim() throws Exception {
        final ClaimOut[] data = {
            new ClaimOut()
                .type("Hello dude")
                .param("author", "yegor256"),
            new ClaimOut()
                .type("Hey")
                .param("flow", "Hey; Hey; Hey"),
            new ClaimOut()
                .type("Validate cause")
                .param("cause", "some text while a number expected"),
            new ClaimOut()
                .type("Validate login")
                .param("login", "this is not a GitHub login"),
            new ClaimOut()
                .type("Validate job")
                .param("job", "this is not a job"),
            new ClaimOut()
                .type("Validate role")
                .param("role", "this is not a role"),
        };
        final Project project = new FkProject();
        final Claims claims = new ClaimsOf(new PropsFarm(), project);
        for (final ClaimOut claim : data) {
            try {
                claim.postTo(claims);
            } catch (final IllegalArgumentException ex) {
                MatcherAssert.assertThat(
                    ex.getLocalizedMessage(),
                    Matchers.containsString(
                        "Failed to transform by "
                    )
                );
            }
        }
    }

}
