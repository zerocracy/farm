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
package com.zerocracy.pmo;

import com.jcabi.aspects.Tv;
import com.zerocracy.Project;
import com.zerocracy.SoftException;
import com.zerocracy.cash.Cash;
import com.zerocracy.farm.fake.FkFarm;
import com.zerocracy.farm.fake.FkProject;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.cactoos.iterable.Filtered;
import org.cactoos.iterable.IterableOf;
import org.cactoos.iterable.LengthOf;
import org.cactoos.iterable.Mapped;
import org.cactoos.iterable.RangeOf;
import org.cactoos.list.ListOf;
import org.cactoos.scalar.And;
import org.cactoos.text.FormattedText;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Test case for {@link People}.
 * @since 1.0
 * @checkstyle JavadocMethodCheck (1000 lines)
 * @checkstyle JavadocVariableCheck (1000 lines)
 * @checkstyle ClassDataAbstractionCouplingCheck (3 lines)
 */
@SuppressWarnings(
    {
        "PMD.AvoidDuplicateLiterals", "PMD.TooManyMethods",
        "PMD.ExcessivePublicCount", "PMD.GodClass"
    }
)
public final class PeopleTest {

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Test
    public void addsAndFindsPeople() throws Exception {
        final FkFarm farm = new FkFarm(new FkProject());
        final People people = new People(farm).bootstrap();
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
        final FkFarm farm = new FkFarm(new FkProject());
        final People people = new People(farm).bootstrap();
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
        final FkFarm farm = new FkFarm(new FkProject());
        final People people = new People(farm).bootstrap();
        final String uid = "dmarkov9";
        MatcherAssert.assertThat(
            people.rate(uid),
            Matchers.equalTo(Cash.ZERO)
        );
    }

    @Test
    public void setsUserWallet() throws Exception {
        final FkFarm farm = new FkFarm(new FkProject());
        final People people = new People(farm).bootstrap();
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
    public void failsForIncorrectBankName() throws Exception {
        final FkFarm farm = new FkFarm(new FkProject());
        final People people = new People(farm).bootstrap();
        final String bank = "test";
        this.thrown.expect(SoftException.class);
        this.thrown.expectMessage(
            new FormattedText("Bank name `%s` is invalid", bank).asString()
        );
        people.wallet("yegor128", bank, "");
    }

    @Test
    public void setsCorrectPaypalAddress() throws Exception {
        PeopleTest.setsWallet("paypal", "john@example.com");
    }

    @Test
    public void setsCorrectBitcoinAddress() throws Exception {
        PeopleTest.setsWallet("btc", "3HcEB6bi4TFPdvk31Pwz77DwAzfAZz2fMn");
    }

    @Test
    public void setCorrectZoldAddress() throws Exception {
        PeopleTest.setsWallet("zld", "g4s8");
    }

    @Test
    public void failsForIncorrectPaypalAddress() throws Exception {
        this.failsWallet("paypal");
    }

    @Test
    public void failsForIncorrectBitcoinAddress() throws Exception {
        this.failsWallet("btc");
    }

    @Test
    public void failsForIncorrectZoldAddress() throws Exception {
        this.failsWallet("zld", "!@#$%fgs");
    }

    @Test
    public void doesnAcceptEthereumAddress() throws Exception {
        this.dontSupportWallet("eth", "Ethereum");
    }

    @Test
    public void doesnAcceptBitcoinCashAddress() throws Exception {
        this.dontSupportWallet("bch", "Bitcoin Cash");
    }

    @Test
    public void doesnAcceptLitecoinAddress() throws Exception {
        this.dontSupportWallet("ltc", "Litecoin");
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
        final FkFarm farm = new FkFarm(project);
        final People people = new People(farm).bootstrap();
        final String uid = "karato90";
        people.wallet(uid, "paypal", "tes1t@example.com");
        people.rate(uid, new Cash.S("$27"));
    }

    @Test
    public void invitesFriend() throws Exception {
        final FkFarm farm = new FkFarm(new FkProject());
        final People people = new People(farm).bootstrap();
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
        final FkFarm farm = new FkFarm(new FkProject());
        final People people = new People(farm).bootstrap();
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
        final FkFarm farm = new FkFarm(new FkProject());
        final People people = new People(farm).bootstrap();
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
        final FkFarm farm = new FkFarm(new FkProject());
        final People people = new People(farm).bootstrap();
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
        final FkFarm farm = new FkFarm(new FkProject());
        final People ppl = new People(farm).bootstrap();
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
        final FkFarm farm = new FkFarm(new FkProject());
        final People people = new People(farm).bootstrap();
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
    public void inviteForce() throws Exception {
        final String mentor = "supermentor";
        final People people = new People(new FkFarm()).bootstrap();
        final int size = 16;
        final String format = "std%d";
        final Iterable<Integer> range = new RangeOf<>(0, size, x -> x + 1);
        new And(
            (String std) -> people.invite(std, mentor, true),
            new Mapped<>((Integer num) -> String.format(format, num), range)
        ).value();
        MatcherAssert.assertThat(
            new LengthOf(
                new Filtered<>(
                    mentor::equals,
                    new Mapped<>(
                        (Integer num) -> people.mentor(
                            String.format(format, num)
                        ),
                        range
                    )
                )
            ).intValue(),
            Matchers.equalTo(size + 1)
        );
    }

    @Test
    public void graduate() throws Exception {
        final FkFarm farm = new FkFarm(new FkProject());
        final People people = new People(farm).bootstrap();
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
        final FkFarm farm = new FkFarm(new FkProject());
        final People people = new People(farm).bootstrap();
        final String uid = "user2345";
        people.invite(uid, uid);
        final int rep = 1024;
        people.reputation(uid, rep);
        MatcherAssert.assertThat(
            people.reputation(uid),
            Matchers.equalTo(rep)
        );
    }

    @Test
    public void remove() throws Exception {
        final FkFarm farm = new FkFarm(new FkProject());
        final People people = new People(farm).bootstrap();
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
        final FkFarm farm = new FkFarm(new FkProject());
        final People people = new People(farm).bootstrap();
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
        final FkFarm farm = new FkFarm(new FkProject());
        final People people = new People(farm).bootstrap();
        final String uid = "user3236";
        final Instant when = Instant.ofEpochMilli(0L);
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
        final FkFarm farm = new FkFarm(new FkProject());
        new People(farm).bootstrap()
            .apply("user124", Instant.ofEpochMilli(0L));
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwIfgetAppliedDateIfNotApplied() throws Exception {
        final FkProject project = new FkProject();
        final FkFarm farm = new FkFarm(project);
        final People people = new People(farm).bootstrap();
        final String uid = "user3236";
        people.invite(uid, uid);
        people.appliedTime(uid);
    }

    @Test
    public void readsEmptyDetails() throws Exception {
        final FkFarm farm = new FkFarm(new FkProject());
        final People people = new People(farm).bootstrap();
        final String uid = "read-empty-details";
        people.invite(uid, uid);
        MatcherAssert.assertThat(
            people.details(uid),
            Matchers.isEmptyString()
        );
    }

    @Test
    public void setsDetails() throws Exception {
        final FkFarm farm = new FkFarm(new FkProject());
        final People people = new People(farm).bootstrap();
        final String uid = "detailed";
        people.invite(uid, uid);
        final String details = "dtls";
        people.details(uid, details);
        MatcherAssert.assertThat(
            people.details(uid),
            Matchers.equalTo(details)
        );
    }

    @Test
    public void setsJobs() throws Exception {
        final People people =
            new People(new FkFarm(new FkProject())).bootstrap();
        final String uid = "jobs";
        people.invite(uid, uid);
        final int jobs = Tv.TEN;
        people.jobs(uid, jobs);
        MatcherAssert.assertThat(
            people.jobs(uid),
            Matchers.is(jobs)
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwsIfFetchingJobsOfNonExistentUser() throws Exception {
        new People(new FkFarm(new FkProject())).bootstrap()
            .jobs("jobs1");
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwsIfSettingJobsOfNonExistentUser() throws Exception {
        new People(new FkFarm(new FkProject())).bootstrap()
            .jobs("jobs2", 1);
    }

    @Test
    public void setsSpeed() throws Exception {
        final People people =
            new People(new FkFarm(new FkProject())).bootstrap();
        final String uid = "speed";
        people.invite(uid, uid);
        final double speed = 5.5;
        people.speed(uid, speed);
        MatcherAssert.assertThat(
            people.speed(uid),
            Matchers.is(speed)
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwsIfFetchingSpeedOfNonExistentUser() throws Exception {
        new People(new FkFarm(new FkProject())).bootstrap()
            .speed("speed1");
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwsIfSettingSpeedOfNonExistentUser() throws Exception {
        new People(new FkFarm(new FkProject())).bootstrap()
            .speed("speed2", 1.0);
    }

    @Test
    public void throwIfTryToSetDetailsButNotApplied() throws Exception {
        final FkFarm farm = new FkFarm(new FkProject());
        final People people = new People(farm).bootstrap();
        final String uid = "not-applied";
        this.thrown.expect(SoftException.class);
        this.thrown.expectMessage(
            Matchers.allOf(
                Matchers.containsString(uid),
                Matchers.containsString("is not with us yet")
            )
        );
        people.details(uid, "ignored");
    }

    @Test
    public void throwIfTryToSetEmptyDetails() throws Exception {
        final FkFarm farm = new FkFarm(new FkProject());
        final People people = new People(farm).bootstrap();
        final String uid = "set-empty-details";
        this.thrown.expect(SoftException.class);
        this.thrown.expectMessage(
            Matchers.allOf(
                Matchers.containsString(uid),
                Matchers.containsString("details can't be empty")
            )
        );
        people.invite(uid, uid);
        people.details(uid, "");
    }

    @Test
    public void setsLinks() throws Exception {
        final FkFarm farm = new FkFarm(new FkProject());
        final People people = new People(farm).bootstrap();
        final String uid = "yegor256";
        final String srel = "slack";
        final String jrel = "jira";
        final String salias = "U67WE3343P";
        final String jalias = "https://www.0crat.com/jira";
        people.link(uid, srel, salias);
        people.link(uid, jrel, jalias);
        final String format = "%s:%s";
        MatcherAssert.assertThat(
            people.links(uid),
            Matchers.containsInAnyOrder(
                String.format(format, srel, salias),
                String.format(format, jrel, jalias),
                String.format("github:%s", uid)
            )
        );
        MatcherAssert.assertThat(
            people.links(uid, jrel),
            Matchers.contains(jalias)
        );
    }

    @Test
    public void setsSkills() throws IOException {
        final People people = new People(
            new FkFarm(new FkProject())
        ).bootstrap();
        final String uid = "user";
        people.invite(uid, "0crat");
        final Iterable<String> skills = new IterableOf<>("c", "cobol");
        people.skills(uid, skills);
        MatcherAssert.assertThat(
            new ArrayList<>(new ListOf<>(people.skills(uid))),
            Matchers.equalTo(new ArrayList<>(new ListOf<>(skills)))
        );
    }

    @Test
    public void getsEmptySkillList() throws IOException {
        final People people = new People(
            new FkFarm(new FkProject())
        ).bootstrap();
        final String uid = "user";
        people.invite(uid, "0crat");
        MatcherAssert.assertThat(
            people.skills(uid),
            Matchers.emptyIterable()
        );
    }

    private void failsWallet(final String bank, final String wallet)
        throws IOException {
        final FkFarm farm = new FkFarm(new FkProject());
        final People people = new People(farm).bootstrap();
        this.thrown.expect(SoftException.class);
        this.thrown.expectMessage(
            new FormattedText(" not valid: `%s`", wallet).asString()
        );
        people.wallet("yegor512", bank, wallet);
    }

    private void dontSupportWallet(final String bank, final String name)
        throws IOException {
        this.thrown.expect(SoftException.class);
        this.thrown.expectMessage(
            String.format("We don't support %s wallets", name)
        );
        new People(new FkFarm()).bootstrap()
            .wallet("yegor512", bank, "123");
    }

    private void failsWallet(final String bank)
        throws IOException {
        this.failsWallet(bank, "123456");
    }

    private static void setsWallet(final String bank, final String wallet)
        throws IOException {
        final FkFarm farm = new FkFarm(new FkProject());
        final People people = new People(farm).bootstrap();
        final String uid = "yegor64";
        people.wallet(uid, bank, wallet);
        MatcherAssert.assertThat(
            people.wallet(uid),
            Matchers.is(wallet)
        );
    }

}
