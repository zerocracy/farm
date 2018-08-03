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

import com.jcabi.xml.XML;
import com.zerocracy.Farm;
import com.zerocracy.Item;
import com.zerocracy.Project;
import com.zerocracy.Xocument;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.cactoos.text.FormattedText;
import org.cactoos.text.JoinedText;
import org.xembly.Directives;

/**
 * Available vacancies.
 *
 * @since 1.0
 */
public final class Vacancies {

    /**
     * Project.
     */
    private final Pmo pmo;

    /**
     * Ctor.
     * @param farm The farm
     */
    public Vacancies(final Farm farm) {
        this(new Pmo(farm));
    }

    /**
     * Ctor.
     * @param pkt Project
     */
    public Vacancies(final Pmo pkt) {
        this.pmo = pkt;
    }

    /**
     * Bootstrap it.
     * @return This
     * @throws IOException If fails
     */
    public Vacancies bootstrap() throws IOException {
        try (final Item item = this.item()) {
            new Xocument(item.path()).bootstrap("pmo/vacancies");
        }
        return this;
    }

    /**
     * Add new vacancy.
     * @param project Hiring project
     * @param author Vacancy author
     * @param text Vacancy text
     * @throws IOException If fails
     */
    public void add(final Project project, final String author,
        final String text) throws IOException {
        this.add(project, author, text, ZonedDateTime.now(ZoneOffset.UTC));
    }

    /**
     * Add new vacancy.
     * @param project Hiring project
     * @param author Vacancy author
     * @param text Vacancy text
     * @param added Added time
     * @throws IOException If fails
     * @checkstyle ParameterNumberCheck (4 lines)
     */
    public void add(final Project project, final String author,
        final String text, final ZonedDateTime added) throws IOException {
        try (final Item item = this.item()) {
            new Xocument(item.path()).modify(
                new Directives()
                    .xpath("/vacancies")
                    .add("vacancy")
                    .attr("project", project.pid())
                    .addIf("added")
                    .set(added.format(DateTimeFormatter.ISO_DATE_TIME))
                    .up()
                    .add("author")
                    .set(author)
                    .up()
                    .add("text")
                    .set(text)
            );
        }
    }

    /**
     * Vacancy for project.
     * @param pid Project id
     * @return Vacancy data
     * @throws IOException If fails
     */
    public XML vacancy(final String pid) throws IOException {
        try (final Item item = this.item()) {
            return new Xocument(item.path())
                .nodes(Vacancies.xpath(pid))
                .get(0);
        }
    }

    /**
     * Remove vacancy.
     * @param project Hiring project
     * @throws IOException If fails
     */
    public void remove(final Project project) throws IOException {
        try (final Item item = this.item()) {
            new Xocument(item.path()).modify(
                new Directives()
                    .xpath(Vacancies.xpath(project.pid()))
                    .remove()
            );
        }
    }

    /**
     * Iterate over all vacancies.
     * @return Vacancies iterable
     * @throws IOException If fails
     */
    public Iterable<String> iterate() throws IOException {
        try (final Item item = this.item()) {
            return new Xocument(item.path())
                .xpath("/vacancies/vacancy/@project");
        }
    }

    /**
     * Remove vacancies older than 'date' param.
     * @param date Date param
     * @return List of project pids that had vacancies removed
     * @throws IOException If fails
     */
    public Iterable<String> removeOlderThan(final Instant date)
        throws IOException {
        try (final Item item = this.item()) {
            final String xpath = new FormattedText(
                "/vacancies/vacancy[xs:dateTime(added) < xs:dateTime('%s')]",
                date
            ).asString();
            final List<String> pids = new Xocument(item.path()).xpath(
                new JoinedText("", xpath, "/@project").asString()
            );
            new Xocument(item.path()).modify(
                new Directives().xpath(xpath).remove()
            );
            return pids;
        }
    }

    /**
     * The item.
     * @return Item
     * @throws IOException If fails
     */
    private Item item() throws IOException {
        return this.pmo.acq("vacancies.xml");
    }

    /**
     * Vacancy xpath.
     *
     * @param pid Project id
     * @return Xpath for vacancy
     */
    private static String xpath(final String pid) {
        return String.format("/vacancies/vacancy[@project='%s']", pid);
    }
}
