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

import com.jcabi.matchers.XhtmlMatchers;
import com.zerocracy.Farm;
import com.zerocracy.Item;
import com.zerocracy.Xocument;
import com.zerocracy.farm.fake.FkFarm;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.ZoneOffset;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.hamcrest.core.IsEqual;
import org.junit.Test;

/**
 * Test case for {@link Resumes}.
 *
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 * @checkstyle MagicNumberCheck (500 lines)
 */
public final class ResumesTest {
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

    @Test(expected = UnsupportedOperationException.class)
    public void findResume() throws Exception {
        final Farm farm = new FkFarm();
        final String login = "login";
        final Instant time = Instant.parse("2018-01-01T00:00:00Z");
        final String text = "Resume text";
        final String personality = "ENTP-T";
        final int id = 187141;
        final String telegram = "telegram";
        final Resume fake = new Resume.Fake(
            time,
            login,
            text,
            personality,
            id,
            telegram
        );
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
            new IsEqual<>(fake)
        );
    }
}
