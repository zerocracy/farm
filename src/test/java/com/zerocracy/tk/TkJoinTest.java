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

import com.jcabi.matchers.XhtmlMatchers;
import com.zerocracy.Farm;
import com.zerocracy.farm.fake.FkFarm;
import com.zerocracy.farm.props.PropsFarm;
import com.zerocracy.pmo.People;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Date;
import org.cactoos.iterable.IterableOf;
import org.cactoos.matchers.TextHasString;
import org.cactoos.text.FormattedText;
import org.cactoos.text.TextOf;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.hamcrest.text.StringContainsInOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.takes.facets.hamcrest.HmRsHeader;
import org.takes.facets.hamcrest.HmRsStatus;
import org.takes.rq.RqFake;
import org.takes.rq.RqWithBody;
import org.takes.rs.RsPrint;

/**
 * Test case for {@link TkJoin}.
 *
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class TkJoinTest {

    @Test
    public void renderJoinPage() throws Exception {
        final Farm farm = new PropsFarm(new FkFarm());
        MatcherAssert.assertThat(
            XhtmlMatchers.xhtml(
                new RsPrint(
                    new TkApp(farm).act(
                        new RqWithUser(farm, new RqFake("GET", "/join"))
                    )
                ).printBody()
            ),
            XhtmlMatchers.hasXPaths("//xhtml:body")
        );
    }

    @Test
    public void acceptRequestAndRedirectOnPost() throws Exception {
        final Farm farm = new PropsFarm(new FkFarm());
        MatcherAssert.assertThat(
            new TkApp(farm).act(
                new RqWithBody(
                    new RqWithUser(
                        farm,
                        new RqFake("POST", "/join-post")
                    ),
                    "personality=INTJ-A&stackoverflow=187141"
                )
            ),
            Matchers.allOf(
                new HmRsStatus(HttpURLConnection.HTTP_SEE_OTHER),
                new HmRsHeader("Location", "/join")
            )
        );
    }

    @Test
    public void rejectsIfAlreadyApplied() throws Exception {
        final Farm farm = new PropsFarm(new FkFarm());
        final People people = new People(farm).bootstrap();
        final String uid = "yegor256";
        people.touch(uid);
        people.apply(uid, new Date());
        final RqWithUser req = new RqWithUser(
            farm,
            new RqFake("POST", "/join-post")
        );
        people.breakup(uid);
        MatcherAssert.assertThat(
            new TkApp(farm).act(
                new RqWithBody(
                    req,
                    "personality=INTJ-A&stackoverflow=187241"
                )
            ),
            new HmRsHeader("Set-Cookie", Matchers.iterableWithSize(2))
        );
    }

    @Test
    public void acceptIfNeverApplied() throws Exception {
        final Farm farm = new PropsFarm(new FkFarm());
        final People people = new People(farm).bootstrap();
        final String uid = "yegor256";
        people.touch(uid);
        final RqWithUser req = new RqWithUser(
            farm,
            new RqFake("POST", "/join-post")
        );
        people.breakup(uid);
        MatcherAssert.assertThat(
            new TkApp(farm).act(
                new RqWithBody(
                    req,
                    // @checkstyle LineLength (1 line)
                    "personality=INTJ-A&stackoverflow=187241&telegram=123&about=txt"
                )
            ),
            Matchers.allOf(
                new HmRsStatus(HttpURLConnection.HTTP_SEE_OTHER),
                new HmRsHeader("Location", "/")
            )
        );
    }

    /**
     * {@link TkJoin} can show that user already has a mentor.
     * {@link TkJoin} must show a message to user when he or she tries to access
     * <code>/join</code> but already has a mentor, which means that the user
     * has already joined or asked for mentor for joining.
     * @throws IOException If something goes wrong accessing page
     */
    @Test
    @Ignore
    public void showsThatUserAlreadyHasMentor() throws IOException {
        final Farm farm = new PropsFarm(new FkFarm());
        final People people = new People(farm).bootstrap();
        final String mentorid = "yoda";
        final String userid = "luke";
        people.touch(mentorid);
        people.touch(userid);
        people.apply(userid, new Date());
        people.invite(userid, mentorid);
        MatcherAssert.assertThat(
            new TextOf(
                new RsPrint(
                    new TkApp(farm).act(
                        new RqWithUser(farm, new RqFake("GET", "/join"))
                    )
                ).printBody()
            ),
            new TextHasString(
                new FormattedText(
                    "User %s is already your mentor, no need to join again",
                    mentorid
                ).asString()
            )
        );
    }

    /**
     * {@link TkJoin} can show resume if user already applied.
     * {@link TkJoin} must show user's resume, if there is one, when user tries
     * to access <code>/join</code> endpoint.
     * @throws IOException If something goes wrong accessing page
     */
    @Test
    @Ignore
    public void showsResumeIfAlreadyApplied() throws Exception {
        final Farm farm = new PropsFarm(new FkFarm());
        final People people = new People(farm).bootstrap();
        final String uid = "yegor256";
        people.touch(uid);
        people.apply(uid, new Date());
        MatcherAssert.assertThat(
            new RsPrint(
                new TkApp(farm).act(
                    new RqWithUser(farm, new RqFake("GET", "/join"))
                )
            ).printBody(),
            new StringContainsInOrder(
                new IterableOf<String>(
                    "User",
                    "here is your resume."
                )
            )
        );
    }
}
