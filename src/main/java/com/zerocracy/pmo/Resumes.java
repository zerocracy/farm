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

import com.zerocracy.Farm;
import com.zerocracy.Item;
import com.zerocracy.Xocument;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.cactoos.text.FormattedText;
import org.xembly.Directives;

/**
 * Resumes.
 * @since 1.0
 *
 * @todo #1569:30min Implement resumes.resume(login), which will return the
 *  resume sent for some user. It will have to return a Resume
 *  implementation which reads the resume from resumes.xml. It must read
 *  /resumes/resume attributes and return them in its methods.
 *  Then remove expected from ResumesTest.findResume so it can be tested to
 *  be used in resume page.
 */
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
        final int stackoverflow, final String telegram) throws IOException {
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
     * Get the user's examiner.
     *
     * @param login Resume author
     * @return Examiner of given user
     * @throws IOException If fails
     */
    public String examiner(final String login) throws IOException {
        try (final Item item = this.item()) {
            return new Xocument(item.path())
                .xpath(
                    String.format(
                        "/resumes/resume[@login='%s']/examiner/text()",
                        login
                    )
                )
                .get(0);
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
        throw new UnsupportedOperationException("resume() not implemented");
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
