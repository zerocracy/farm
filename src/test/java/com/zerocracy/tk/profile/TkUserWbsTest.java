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
package com.zerocracy.tk.profile;

import com.zerocracy.Farm;
import com.zerocracy.farm.fake.FkFarm;
import com.zerocracy.farm.fake.FkProject;
import com.zerocracy.farm.props.PropsFarm;
import com.zerocracy.pmo.People;
import com.zerocracy.pmo.Projects;
import com.zerocracy.tk.RqWithUser;
import com.zerocracy.tk.TkApp;
import org.cactoos.list.ListOf;
import org.cactoos.text.FormattedText;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.StringContains;
import org.junit.Test;
import org.takes.rq.RqFake;
import org.takes.rs.RsPrint;

/**
 * Test case for {@link TkUserWbs}.
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class TkUserWbsTest {

    @Test
    public void rendersUserProjectPid() throws Exception {
        final FkProject project = new FkProject();
        final Farm farm = new PropsFarm(new FkFarm(project));
        final String uid = "krzyk";
        final People people = new People(farm).bootstrap();
        people.touch(uid);
        people.invite(uid, "yegor256");
        new Projects(farm, uid).bootstrap().add(project.pid());
        MatcherAssert.assertThat(
            new RsPrint(
                new TkApp(farm).act(
                    new RqWithUser(
                        farm,
                        new RqFake(
                            new ListOf<>(
                                new FormattedText("GET /u/%s/wbs", uid)
                                    .asString(),
                                "Host: www.example.com",
                                "Accept: application/vnd.zerocracy+xml"
                            ),
                            ""
                        ),
                        uid,
                        false
                    )
                )
            ).printBody(),
            new StringContains(project.pid())
        );
    }
}
