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
package com.zerocracy.tk;

import com.jcabi.aspects.Tv;
import com.jcabi.matchers.XhtmlMatchers;
import com.zerocracy.Farm;
import com.zerocracy.farm.fake.FkFarm;
import com.zerocracy.farm.fake.FkProject;
import com.zerocracy.farm.props.PropsFarm;
import com.zerocracy.pmo.Awards;
import com.zerocracy.pmo.People;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import org.cactoos.list.ListOf;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.takes.rq.RqFake;
import org.takes.rs.RsPrint;

/**
 * Test case for {@link TkTeam}.
 * @since 1.0
 * @checkstyle JavadocMethod (500 lines)
 * @checkstyle MagicNumber (500 lines)
 * @checkstyle ClassDataAbstractionCoupling (3 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class TkTeamTest {

    @Test
    public void showsSpeedInDays() throws Exception {
        final Farm farm = new PropsFarm(new FkFarm());
        final People people = new People(farm).bootstrap();
        final String user = "yegor256";
        people.touch(user);
        people.speed(user, 2880.0);
        new Awards(farm, user).bootstrap().add(
            new FkProject(), 1, "none", "reason", new Date()
        );
        MatcherAssert.assertThat(
            XhtmlMatchers.xhtml(this.responseBody(farm)),
            XhtmlMatchers.hasXPaths(
                String.format("//xhtml:td[.='%s']", 2.0)
            )
        );
    }

    @Test
    public void showsNumberOfJobsInAgenda() throws Exception {
        final Farm farm = new PropsFarm(new FkFarm());
        final People people = new People(farm).bootstrap();
        final String user = "yegor256";
        people.touch(user);
        new Awards(farm, user).bootstrap().add(
            new FkProject(), 1, "none", "reason", new Date()
        );
        people.jobs(user, Tv.TEN);
        MatcherAssert.assertThat(
            XhtmlMatchers.xhtml(this.responseBody(farm)),
            XhtmlMatchers.hasXPaths(
                String.format("//xhtml:td[.='%s']", Tv.TEN)
            )
        );
    }

    @Test
    public void showsActiveUsers() throws Exception {
        final Farm farm = new PropsFarm(new FkFarm());
        final People people = new People(farm).bootstrap();
        final String active = "g4s8";
        people.touch(active);
        people.invite(active, "yegor256");
        new Awards(farm, active).bootstrap().add(
            new FkProject(), 1, "none", "reason1", new Date()
        );
        final String inactive = "krzyk";
        people.touch(inactive);
        people.invite(inactive, "yegor256");
        new Awards(farm, inactive).bootstrap().add(
            new FkProject(), 2, "none", "reason2",
            new Date(Instant.now().minus(Duration.ofDays(91)).toEpochMilli())
        );
        MatcherAssert.assertThat(
            XhtmlMatchers.xhtml(this.responseBody(farm)),
            Matchers.allOf(
                XhtmlMatchers.hasXPaths(
                    String.format("//xhtml:a[.='@%s']", active)
                ),
                Matchers.not(
                    XhtmlMatchers.hasXPaths(
                        String.format("//xhtml:a[.='@%s']", inactive)
                    )
                )
            )
        );
    }

    private String responseBody(final Farm farm) throws IOException {
        return new RsPrint(
            new TkApp(farm).act(
                new RqWithUser(
                    farm,
                    new RqFake(
                        new ListOf<>(
                            "GET /team",
                            "Host: www.example.com"
                        ),
                        ""
                    )
                )
            )
        ).printBody();
    }
}
