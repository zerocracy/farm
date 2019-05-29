/*
 * Copyright (c) 2016-2019 Zerocracy
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

import com.jcabi.matchers.XhtmlMatchers;
import com.zerocracy.Farm;
import com.zerocracy.Item;
import com.zerocracy.Xocument;
import com.zerocracy.farm.fake.FkFarm;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.ZoneOffset;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.hamcrest.core.IsEqual;
import org.hamcrest.core.IsNot;
import org.hamcrest.core.IsNull;
import org.junit.Test;
import org.xembly.Xembler;

/**
 * Test case for {@link Resumes}.
 *
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 * @checkstyle MagicNumberCheck (500 lines)
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@SuppressWarnings({"PMD.AvoidDuplicateLiterals", "PMD.TooManyMethods"})
public final class ResumesTest {

    /**
     * Personality type.
     */
    private static final String PERSONALITY = "INTJ-A";

    @Test
    public void addsResumes() throws Exception {
        final Farm farm = new FkFarm();
        new Resumes(farm).bootstrap()
            .add(
                "new",
                LocalDateTime.of(
                    LocalDate.of(2018, Month.JANUARY, 1),
                    LocalTime.of(0, 0)
                ),
                "Invite me",
                "INTJ-A",
                187141,
                "test"
            );
        try (final Item item = new Pmo(farm).acq("resumes.xml")) {
            MatcherAssert.assertThat(
                new Xocument(item).nodes("/resumes/resume[@login = 'new']"),
                Matchers.contains(
                    XhtmlMatchers.hasXPaths(
                        "resume/text[text() = 'Invite me']",
                        "resume/submitted[text() = '2018-01-01T00:00:00']",
                        "resume/personality[text() = 'INTJ-A']",
                        "resume/stackoverflow[text() = '187141']"
                    )
                )
            );
        }
    }

    @Test
    public void findUnassignedResumes() throws Exception {
        final Farm farm = new FkFarm();
        final Resumes resumes = new Resumes(farm).bootstrap();
        final String first = "first";
        resumes.add(
            first,
            LocalDateTime.of(
                LocalDate.of(2017, Month.FEBRUARY, 2),
                LocalTime.of(0, 0)
            ),
            "I'm new java programmer here",
            "INTJ-T",
            151234,
            "@first"
        );
        final String second = "second";
        resumes.add(
            second,
            LocalDateTime.of(
                LocalDate.of(2016, Month.MARCH, 3),
                LocalTime.of(0, 0)
            ),
            "I'm old java programmer here",
            "ENTJ-A",
            257145,
            "@second"
        );
        resumes.assign(second, "examiner");
        MatcherAssert.assertThat(
            resumes.unassigned(),
            Matchers.contains(first)
        );
    }

    @Test
    public void findResume() throws Exception {
        final Farm farm = new FkFarm();
        final String login = "login";
        final Instant time = Instant.parse("2018-01-01T00:00:00Z");
        final String text = "Resume text";
        final String personality = "ENTP-T";
        final long id = 187141;
        final String telegram = "telegram";
        final Resumes resumes = new Resumes(farm).bootstrap();
        resumes.add(
            login,
            LocalDateTime.ofInstant(time, ZoneOffset.UTC),
            text,
            personality,
            id,
            telegram
        );
        MatcherAssert.assertThat(
            "Could not find resume",
            resumes.resume(login),
            new IsNot<>(new IsNull<>())
        );
    }

    @Test
    public void resumeExists() throws Exception {
        final Farm farm = new FkFarm();
        final String login = "resumeexists";
        final Instant time = Instant.parse("2018-02-01T00:00:00Z");
        final String text = "This resume exists in farm";
        final String personality = ResumesTest.PERSONALITY;
        final long id = 187141;
        final String telegram = "existentresumetelegram";
        final Resumes resumes = new Resumes(farm).bootstrap();
        resumes.add(
            login,
            LocalDateTime.ofInstant(time, ZoneOffset.UTC),
            text,
            personality,
            id,
            telegram
        );
        MatcherAssert.assertThat(
            "Resume exists but resumes did not found it",
            resumes.exists(login),
            new IsEqual<>(true)
        );
    }

    @Test
    public void resumeDoesNotExists() throws Exception {
        final Farm farm = new FkFarm();
        final String login = "resumedoesnotexists";
        final Resumes resumes = new Resumes(farm).bootstrap();
        MatcherAssert.assertThat(
            "Resume does not exist but resumes found it",
            resumes.exists(login),
            new IsEqual<>(false)
        );
    }

    @Test
    public void resumeHasLoginAttribute() throws Exception {
        final Farm farm = new FkFarm();
        final String login = "a-login";
        final Instant time = Instant.parse("2018-01-01T00:00:00Z");
        final String text = "Resume text";
        final String personality = "ENTP-T";
        final long id = 187141;
        final String telegram = "telegram";
        final Resumes resumes = new Resumes(farm).bootstrap();
        resumes.add(
            login,
            LocalDateTime.ofInstant(time, ZoneOffset.UTC),
            text,
            personality,
            id,
            telegram
        );
        MatcherAssert.assertThat(
            "Could not find resume",
            resumes.resume(login).login(),
            new IsEqual<>(login)
        );
    }

    @Test
    public void resumeHasSubmittedTime() throws Exception {
        final Farm farm = new FkFarm();
        final String login = "a-login";
        final Instant time = Instant.parse("2018-01-01T00:00:00Z");
        final String text = "Resume text";
        final String personality = "ENTP-T";
        final long id = 187141;
        final String telegram = "telegram";
        final Resumes resumes = new Resumes(farm).bootstrap();
        resumes.add(
            login,
            LocalDateTime.ofInstant(time, ZoneOffset.UTC),
            text,
            personality,
            id,
            telegram
        );
        MatcherAssert.assertThat(
            "Could not find resume",
            resumes.resume(login).submitted(),
            new IsEqual<>(time)
        );
    }

    @Test
    public void resumeHasText() throws Exception {
        final Farm farm = new FkFarm();
        final String login = "a-login";
        final Instant time = Instant.parse("2018-01-01T00:00:00Z");
        final String text = "Resume text";
        final String personality = "ENTP-T";
        final long id = 187141;
        final String telegram = "telegram";
        final Resumes resumes = new Resumes(farm).bootstrap();
        resumes.add(
            login,
            LocalDateTime.ofInstant(time, ZoneOffset.UTC),
            text,
            personality,
            id,
            telegram
        );
        MatcherAssert.assertThat(
            "Could not find resume",
            resumes.resume(login).text(),
            new IsEqual<>(text)
        );
    }

    @Test
    public void resumeHasPersonality() throws Exception {
        final Farm farm = new FkFarm();
        final String login = "a-login";
        final Instant time = Instant.parse("2018-01-01T00:00:00Z");
        final String text = "Resume text";
        final String personality = "ENTP-T";
        final long id = 187141;
        final String telegram = "telegram";
        final Resumes resumes = new Resumes(farm).bootstrap();
        resumes.add(
            login,
            LocalDateTime.ofInstant(time, ZoneOffset.UTC),
            text,
            personality,
            id,
            telegram
        );
        MatcherAssert.assertThat(
            "Could not find resume",
            resumes.resume(login).personality(),
            new IsEqual<>(personality)
        );
    }

    @Test
    public void resumeHasLoginStackOverflowId() throws Exception {
        final Farm farm = new FkFarm();
        final String login = "a-login";
        final Instant time = Instant.parse("2018-01-01T00:00:00Z");
        final String text = "Resume text";
        final String personality = "ENTP-T";
        final long id = 187141;
        final String telegram = "telegram";
        final Resumes resumes = new Resumes(farm).bootstrap();
        resumes.add(
            login,
            LocalDateTime.ofInstant(time, ZoneOffset.UTC),
            text,
            personality,
            id,
            telegram
        );
        MatcherAssert.assertThat(
            "Could not find resume",
            resumes.resume(login).soid(),
            new IsEqual<>(id)
        );
    }

    @Test
    public void resumeHasLoginTelegramId() throws Exception {
        final Farm farm = new FkFarm();
        final String login = "a-login";
        final Instant time = Instant.parse("2018-01-01T00:00:00Z");
        final String text = "Resume text";
        final String personality = "ENTP-T";
        final long id = 187141;
        final String telegram = "atelegramid";
        final Resumes resumes = new Resumes(farm).bootstrap();
        resumes.add(
            login,
            LocalDateTime.ofInstant(time, ZoneOffset.UTC),
            text,
            personality,
            id,
            telegram
        );
        MatcherAssert.assertThat(
            "Could not find resume",
            resumes.resume(login).telegram(),
            new IsEqual<>(telegram)
        );
    }

    @Test
    public void filterByExaminer() throws Exception {
        final Farm farm = new FkFarm();
        final String login = "test25437";
        final Resumes resumes = new Resumes(farm).bootstrap();
        resumes.add(
            login, LocalDateTime.now(), "test resume 1245",
            ResumesTest.PERSONALITY, 761536247L, "qwrtsfg"
        );
        resumes.assign(login, "g4s8");
        MatcherAssert.assertThat(
            new Xembler(resumes.filter("examiner = *")).xml(),
            XhtmlMatchers.hasXPaths(
                "/resumes/resume[@login = 'test25437']"
            )
        );
    }

    @Test
    public void filterOlderThan() throws Exception {
        final Farm farm = new FkFarm();
        final Resumes resumes = new Resumes(farm).bootstrap();
        final String target = "olduser";
        resumes.add(
            target,
            LocalDateTime.MIN,
            "invite my please12561",
            ResumesTest.PERSONALITY,
            2652134263L,
            "tyrte54t4"
        );
        resumes.add(
            "newuser",
            LocalDateTime.now(),
            "new user 125",
            ResumesTest.PERSONALITY,
            65435364L,
            "ghgdf456"
        );
        MatcherAssert.assertThat(
            resumes.olderThan(Instant.now().minus(Duration.ofDays(1L))),
            Matchers.contains(target)
        );
    }

    @Test
    public void resumeHasExaminer() throws Exception {
        final Farm farm = new FkFarm();
        final String login = "a-login";
        final Instant time = Instant.parse("2018-01-01T00:00:00Z");
        final String text = "Resume text";
        final String personality = "ENTP-T";
        final long id = 187141;
        final String telegram = "telegram";
        final Resumes resumes = new Resumes(farm).bootstrap();
        resumes.add(
            login,
            LocalDateTime.ofInstant(time, ZoneOffset.UTC),
            text,
            personality,
            id,
            telegram
        );
        resumes.assign(login, "g4s8");
        MatcherAssert.assertThat(
            "The resume doesn't have an examiner",
            resumes.hasExaminer(login),
            Matchers.is(true)
        );
    }

    @Test
    public void resumeDoesntHaveExaminer() throws Exception {
        final Farm farm = new FkFarm();
        final String login = "a-login";
        final Instant time = Instant.parse("2018-01-01T00:00:00Z");
        final String text = "Resume text";
        final String personality = "ENTP-T";
        final long id = 187141;
        final String telegram = "telegram";
        final Resumes resumes = new Resumes(farm).bootstrap();
        resumes.add(
            login,
            LocalDateTime.ofInstant(time, ZoneOffset.UTC),
            text,
            personality,
            id,
            telegram
        );
        MatcherAssert.assertThat(
            "The resume has an examiner",
            resumes.hasExaminer(login),
            Matchers.is(false)
        );
    }
}
