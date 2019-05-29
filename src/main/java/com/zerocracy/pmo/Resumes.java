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

import com.jcabi.xml.XML;
import com.zerocracy.Farm;
import com.zerocracy.Item;
import com.zerocracy.Xocument;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.cactoos.text.FormattedText;
import org.xembly.Directive;
import org.xembly.Directives;

/**
 * Resumes.
 * @since 1.0
 */
@SuppressWarnings({"PMD.AvoidDuplicateLiterals", "PMD.TooManyMethods"})
public final class Resumes {

    /**
     * PMO.
     */
    private final Pmo pmo;

    /**
     * Ctor.
     * @param farm The farm
     */
    public Resumes(final Farm farm) {
        this(new Pmo(farm));
    }

    /**
     * Ctor.
     * @param pkt PMO
     */
    public Resumes(final Pmo pkt) {
        this.pmo = pkt;
    }

    /**
     * Bootstrap it.
     * @return This
     * @throws IOException If fails
     */
    public Resumes bootstrap() throws IOException {
        try (final Item item = this.item()) {
            new Xocument(item.path()).bootstrap("pmo/resumes");
        }
        return this;
    }

    /**
     * Add resume.
     * @param login Resume author
     * @param when When submitted
     * @param text Resume text
     * @param personality Author's personality
     * @param stackoverflow Stackoverflow id
     * @param telegram Telegram username
     * @throws IOException If fails
     * @checkstyle ParameterNumberCheck (5 lines)
     */
    @SuppressWarnings("PMD.UseObjectForClearerAPI")
    public void add(final String login, final LocalDateTime when,
        final String text, final String personality,
        final long stackoverflow, final String telegram) throws IOException {
        try (final Item item = this.item()) {
            new Xocument(item).modify(
                new Directives()
                    .xpath("/resumes")
                    .add("resume")
                    .attr("login", login)
                    .add("submitted")
                    .set(when.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                    .up()
                    .add("text")
                    .set(text)
                    .up()
                    .add("personality")
                    .set(personality)
                    .up()
                    .add("stackoverflow")
                    .set(stackoverflow)
                    .up()
                    .add("telegram")
                    .set(telegram)
            );
        }
    }

    /**
     * All not assigned yet resumes.
     *
     * @return Login list
     * @throws IOException If fails
     */
    public Iterable<String> unassigned() throws IOException {
        try (final Item item = this.item()) {
            return new Xocument(item)
                .xpath("/resumes/resume[not(./examiner)]/@login");
        }
    }

    /**
     * All resumes.
     *
     * @return Login list
     * @throws IOException If fails
     */
    public Iterable<String> all() throws IOException {
        try (final Item item = this.item()) {
            return new Xocument(item).xpath("/resumes/resume/@login");
        }
    }

    /**
     * Assign examiner to resume.
     *
     * @param login Resume author
     * @param examiner Examiner
     * @throws IOException If fails
     */
    public void assign(final String login, final String examiner)
        throws IOException {
        try (final Item item = this.item()) {
            new Xocument(item).modify(
                new Directives()
                    .xpath(
                        String.format("/resumes/resume[@login = '%s']", login)
                    ).addIf("examiner")
                    .set(examiner)
            );
        }
    }

    /**
     * This resume has a examiner?
     * @param login Resume author
     * @return TRUE if it has a examiner
     * @throws IOException If fails
     */
    public boolean hasExaminer(final String login) throws IOException {
        try (final Item item = this.item()) {
            return !new Xocument(item.path()).nodes(
                String.format(
                    "/resumes/resume[@login='%s']/examiner",
                    login
                )
            ).isEmpty();
        }
    }

    /**
     * Get the user's examiner.
     *
     * @param login Resume author
     * @return Examiner of given user
     * @throws IOException If fails
     */
    public String examiner(final String login) throws IOException {
        try (final Item item = this.item()) {
            return new Xocument(item.path()).xpath(
                String.format(
                    "/resumes/resume[@login='%s']/examiner/text()",
                    login
                )
            ).get(0);
        }
    }

    /**
     * Remove a resume.
     *
     * @param login Resume author
     * @throws IOException If fails
     */
    public void remove(final String login) throws IOException {
        try (final Item item = this.item()) {
            new Xocument(item.path()).modify(
                new Directives().xpath(
                    new FormattedText(
                        "/resumes/resume[@login='%s']",
                        login
                    ).asString()
                ).remove()
            );
        }
    }

    /**
     * Returns the {@link Resume} from user.
     *
     * @param login Resume author login
     * @return Resume from author
     * @throws IOException If fails or resume not found
     */
    public Resume resume(final String login) throws IOException {
        try (final Item item = this.item()) {
            return new ResumeXml(
                new Xocument(item.path()).nodes(
                    new FormattedText(
                        "/resumes/resume[@login='%s' ]",
                        login
                    ).asString()
                ).get(0)
            );
        }
    }

    /**
     * Resume exists?
     * @param login User login
     * @return TRUE if it exists
     * @throws IOException If fails
     */
    public boolean exists(final String login) throws IOException {
        try (final Item item = this.item()) {
            return !new Xocument(item.path()).nodes(
                new FormattedText(
                    "/resumes/resume[@login='%s' ]",
                    login
                ).asString()
            ).isEmpty();
        }
    }

    /**
     * Filter resumes by expression.
     * @param expr Expression for resume
     * @return Resumes
     * @throws IOException If fails
     */
    public Iterable<Directive> filter(final String expr) throws IOException {
        final Directives dirs = new Directives();
        try (final Item item = this.item()) {
            final List<XML> nodes = new Xocument(item.path()).nodes(
                String.format("/resumes/resume[%s]", expr)
            );
            dirs.add("resumes");
            for (final XML node : nodes) {
                dirs.add("resume")
                    .attr("login", node.xpath("@login").get(0))
                    .add("text")
                    .set(node.xpath("text/text()").get(0))
                    .up()
                    .add("personality")
                    .set(node.xpath("personality/text()").get(0))
                    .up()
                    .add("stackoverflow")
                    .set(node.xpath("stackoverflow/text()").get(0))
                    .up()
                    .add("telegram")
                    .set(node.xpath("telegram/text()").get(0))
                    .up()
                    .add("examiner")
                    .set(node.xpath("examiner/text()").get(0))
                    .up()
                    .add("submitted")
                    .set(node.xpath("submitted/text()").get(0))
                    .up()
                    .up();
            }
            dirs.up();
        }
        return dirs;
    }

    /**
     * Find all resumes older than time.
     * @param time Time to filter
     * @return Resume logins
     * @throws IOException If fails
     */
    public Iterable<String> olderThan(final Instant time) throws IOException {
        try (final Item item = this.item()) {
            return new Xocument(item.path()).xpath(
                String.join(
                    "",
                    "/resumes/resume[",
                    "xs:dateTime(submitted) < xs:dateTime('",
                    time.toString(),
                    "')]/@login"
                )
            );
        }
    }

    /**
     * The item.
     * @return Item
     * @throws IOException If fails
     */
    private Item item() throws IOException {
        return this.pmo.acq("resumes.xml");
    }
}
