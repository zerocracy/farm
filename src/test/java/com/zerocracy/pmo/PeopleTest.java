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
package com.zerocracy.pmo;

import com.zerocracy.jstk.Project;
import com.zerocracy.jstk.cash.Cash;
import com.zerocracy.jstk.fake.FkProject;
import java.nio.file.Files;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link People}.
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.1
 * @checkstyle JavadocMethodCheck (500 lines)
 */
public final class PeopleTest {

    @Test
    public void addsAndFindsPeople() throws Exception {
        final People people = new People(new FkProject()).bootstrap();
        final String uid = "yegor256";
        final String rel = "slack";
        final String alias = "U67WE3343P";
        people.link(uid, rel, alias);
        people.link(uid, "jira", "http://www.0crat.com/jira");
        MatcherAssert.assertThat(
            people.find(rel, alias),
            Matchers.not(Matchers.emptyIterable())
        );
        MatcherAssert.assertThat(
            people.links(uid),
            Matchers.hasItem("slack:U67WE3343P")
        );
    }

    @Test
    public void setsUserRate() throws Exception {
        final People people = new People(new FkProject()).bootstrap();
        final String uid = "alex-palevsky";
        people.rate(uid, new Cash.S("$35"));
        people.rate(uid, new Cash.S("$50"));
        MatcherAssert.assertThat(
            people.rate(uid),
            Matchers.equalTo(new Cash.S("USD 50"))
        );
    }

    @Test
    public void readsUnsetRate() throws Exception {
        final People people = new People(new FkProject()).bootstrap();
        final String uid = "dmarkov9";
        MatcherAssert.assertThat(
            people.rate(uid),
            Matchers.equalTo(Cash.ZERO)
        );
    }

    @Test
    public void setsUserWallet() throws Exception {
        final People people = new People(new FkProject()).bootstrap();
        final String uid = "yegor256-1";
        people.wallet(uid, "btc", "68739473849732849732849327");
        people.wallet(uid, "paypal", "yegor256@gmail.com");
        MatcherAssert.assertThat(
            people.wallet(uid),
            Matchers.startsWith("yegor256@")
        );
        MatcherAssert.assertThat(
            people.bank(uid),
            Matchers.startsWith("payp")
        );
    }

    @Test
    public void setsAndFetchesUserSkills() throws Exception {
        final People people = new People(new FkProject()).bootstrap();
        final String uid = "karato";
        final String skill = "java";
        people.skill(uid, skill);
        people.skill(uid, "java.spring");
        people.skill(uid, "ruby");
        MatcherAssert.assertThat(
            people.skills(uid),
            Matchers.hasItem(skill)
        );
    }

    @Test
    public void upgradesXsdAutomatically() throws Exception {
        final Project project = new FkProject();
        Files.write(
            project.acq("people.xml").path(),
            String.join(
                "",
                "<people xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'",
                " xsi:noNamespaceSchemaLocation='",
                "https://raw.githubusercontent.com/zerocracy/datum/0.7.1",
                "/xsd/project/people.xsd'/>"
            ).getBytes()
        );
        final People people = new People(project).bootstrap();
        people.skill("karato90", "java9");
    }

    @Test
    public void invitesFriend() throws Exception {
        final People people = new People(new FkProject()).bootstrap();
        final String uid = "jack";
        final String friend = "friend";
        people.invite(friend, uid);
        people.invite("another-friend", uid);
        MatcherAssert.assertThat(
            people.hasMentor(friend),
            Matchers.is(true)
        );
    }

    @Test
    public void vacationTest() throws Exception {
        final People people = new People(new FkProject()).bootstrap();
        final String uid = "g4s8";
        MatcherAssert.assertThat(
            people.vacation(uid),
            Matchers.is(false)
        );
        people.vacation(uid, true);
        MatcherAssert.assertThat(
            people.vacation(uid),
            Matchers.is(true)
        );
        people.vacation(uid, false);
        MatcherAssert.assertThat(
            people.vacation(uid),
            Matchers.is(false)
        );
    }
}
