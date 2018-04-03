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
package com.zerocracy.pmo;

import com.zerocracy.Project;
import com.zerocracy.SoftException;
import com.zerocracy.cash.Cash;
import com.zerocracy.farm.fake.FkProject;
import java.nio.file.Files;
import java.util.Date;
import java.util.List;
import org.cactoos.iterable.Mapped;
import org.cactoos.iterable.RangeOf;
import org.cactoos.list.ListOf;
import org.cactoos.scalar.And;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link People}.
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.1
 * @checkstyle JavadocMethodCheck (500 lines)
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 * @todo #552:30min Add tests for details(), links(), wallet() and rate()
 *  in People.java because these methods are not fully covered. Then
 *  remove People.java from jacoco excludes in pom.xml
 */
@SuppressWarnings({"PMD.AvoidDuplicateLiterals", "PMD.TooManyMethods"})
public final class PeopleTest {

    @Test
    public void addsAndFindsPeople() throws Exception {
        final People people = new People(new FkProject()).bootstrap();
        final String uid = "yegor256";
        final String rel = "slack";
        final String alias = "U67WE3343P";
        people.link(uid, rel, alias);
        people.link(uid, "jira", "https://www.0crat.com/jira");
        MatcherAssert.assertThat(
            people.iterate(),
            Matchers.hasItem(uid)
        );
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
        people.wallet(uid, "paypal", "test@example.com");
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
    public void upgradesXsdAutomatically() throws Exception {
        final Project project = new FkProject();
        Files.write(
            project.acq("people.xml").path(),
            String.join(
                "",
                "<people xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'",
                " xsi:noNamespaceSchemaLocation='",
                "http://datum.zerocracy.com/0.7.1",
                "/xsd/project/people.xsd'/>"
            ).getBytes()
        );
        final People people = new People(project).bootstrap();
        final String uid = "karato90";
        people.wallet(uid, "paypal", "tes1t@example.com");
        people.rate(uid, new Cash.S("$27"));
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

    @Test
    public void breakupTest() throws Exception {
        final People people = new People(new FkProject()).bootstrap();
        final String uid = "john";
        final String friend = "jimmy";
        people.invite(friend, uid);
        people.breakup(friend);
        MatcherAssert.assertThat(
            people.hasMentor(friend),
            Matchers.is(false)
        );
    }

    @Test
    public void mentorTest() throws Exception {
        final People people = new People(new FkProject()).bootstrap();
        final String uid = "datum";
        final String mentor = "0crat";
        people.invite(uid, mentor);
        MatcherAssert.assertThat(
            people.mentor(uid),
            Matchers.equalTo(mentor)
        );
    }

    @Test
    public void studentsTest() throws Exception {
        final People ppl = new People(new FkProject()).bootstrap();
        final String mentor = "mentor";
        final List<String> students = new ListOf<>(
            "student1",
            "student2",
            "student3"
        );
        new And((String std) -> ppl.invite(std, mentor), students).value();
        MatcherAssert.assertThat(
            ppl.students(mentor),
            Matchers.allOf(
                Matchers.iterableWithSize(students.size()),
                Matchers.hasItems(
                    students.toArray(new String[students.size()])
                )
            )
        );
    }

    @Test(expected = SoftException.class)
    public void inviteSixteen() throws Exception {
        final String mentor = "mnt";
        final People people = new People(new FkProject()).bootstrap();
        new And(
            (String std) -> people.invite(std, mentor),
            new Mapped<>(
                (Integer num) -> String.format("student%d", num),
                // @checkstyle MagicNumber (1 line)
                new RangeOf<>(0, 16, x -> x + 1)
            )
        ).value();
    }

    @Test
    public void graduate() throws Exception {
        final People people = new People(new FkProject()).bootstrap();
        final String uid = "yegor11";
        people.invite(uid, "the-mentor");
        people.graduate(uid);
        MatcherAssert.assertThat(
            people.mentor(uid),
            Matchers.equalTo("0crat")
        );
    }

    @Test
    public void reputation() throws Exception {
        final People people = new People(new FkProject()).bootstrap();
        final String uid = "user2345";
        people.invite(uid, uid);
        final int rep = 1024;
        people.reputation(uid, rep);
        MatcherAssert.assertThat(
            people.reputation(uid),
            Matchers.equalTo(rep)
        );
    }

    public void remove() throws Exception {
        final People people = new People(new FkProject()).bootstrap();
        final String uid = "remove";
        people.invite(uid, "mentor11");
        people.remove(uid);
        MatcherAssert.assertThat(
            people.iterate(),
            Matchers.emptyIterable()
        );
    }

    @Test
    public void getSingleLink() throws Exception {
        final People people = new People(new FkProject()).bootstrap();
        final String uid = "linker";
        people.invite(uid, uid);
        final String rel = "some-rel11";
        final String href = "some-href22";
        people.link(uid, rel, href);
        MatcherAssert.assertThat(
            people.link(uid, rel),
            Matchers.equalTo(href)
        );
    }

    @Test
    public void canApply() throws Exception {
        final People people = new People(new FkProject()).bootstrap();
        final String uid = "user3236";
        final Date when = new Date(0L);
        people.invite(uid, uid);
        people.apply(uid, when);
        MatcherAssert.assertThat(
            "applied",
            people.applied(uid),
            Matchers.is(true)
        );
        MatcherAssert.assertThat(
            "applied time",
            people.appliedTime(uid),
            Matchers.equalTo(when)
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwIfApplyButDoesntExist() throws Exception {
        new People(new FkProject()).bootstrap()
            .apply("user124", new Date(0L));
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwIfgetAppliedDateIfNotApplied() throws Exception {
        final People people = new People(new FkProject()).bootstrap();
        final String uid = "user3236";
        people.invite(uid, uid);
        people.appliedTime(uid);
    }
}
