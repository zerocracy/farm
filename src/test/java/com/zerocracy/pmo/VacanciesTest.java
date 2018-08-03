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
import com.zerocracy.Project;
import com.zerocracy.farm.fake.FkFarm;
import com.zerocracy.farm.fake.FkProject;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link Vacancies}.
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 */
public final class VacanciesTest {
    @Test
    public void addsVacancy() throws Exception {
        final Vacancies vacancies = new Vacancies(new FkFarm()).bootstrap();
        final Project project = new FkProject();
        vacancies.add(project, "test", "unit test is hiring");
        MatcherAssert.assertThat(
            vacancies.iterate(),
            Matchers.contains(Matchers.equalTo(project.pid()))
        );
    }

    @Test
    public void removesVacancy() throws Exception {
        final Vacancies vacancies = new Vacancies(new FkFarm()).bootstrap();
        final Project project = new FkProject();
        vacancies.add(project, "del", "to remove");
        vacancies.remove(project);
        MatcherAssert.assertThat(
            vacancies.iterate(),
            Matchers.emptyIterable()
        );
    }

    @Test
    public void removeOldVacancies() throws Exception {
        final Vacancies vacancies = new Vacancies(new FkFarm()).bootstrap();
        final Project first = new FkProject("FAKEPRJC1");
        final FkProject second = new FkProject("FAKEPRJC2");
        // @checkstyle MagicNumberCheck (30 lines)
        vacancies.add(
            first,
            "old",
            "old vacancy",
            ZonedDateTime.of(
                LocalDate.of(2018, Month.JANUARY, 1),
                LocalTime.of(0, 0),
                ZoneOffset.UTC
            )
        );
        vacancies.add(
            second,
            "fresh",
            "fresh vacancy",
            ZonedDateTime.of(
                LocalDate.of(2018, Month.JUNE, 1),
                LocalTime.of(0, 0),
                ZoneOffset.UTC
            )
        );
        final Iterable<String> pids = vacancies.removeOlderThan(
            ZonedDateTime.of(
                LocalDate.of(2018, Month.APRIL, 1),
                LocalTime.of(0, 0),
                ZoneOffset.UTC
            ).toInstant()
        );
        MatcherAssert.assertThat(
            pids,
            Matchers.contains(first.pid())
        );
        MatcherAssert.assertThat(
            vacancies.iterate(),
            Matchers.contains(second.pid())
        );
    }

    @Test
    public void renderVacancy() throws Exception {
        final Vacancies vacancies = new Vacancies(new FkFarm()).bootstrap();
        final Project project = new FkProject();
        final String author = "user125";
        final String text = "vacancy text 123";
        vacancies.add(project, author, text);
        MatcherAssert.assertThat(
            vacancies.vacancy(project.pid()),
            XhtmlMatchers.hasXPaths(
                String.format("/vacancy/author[text() = '%s']", author),
                String.format("/vacancy/text[text() = '%s']", text)
            )
        );
    }
}
