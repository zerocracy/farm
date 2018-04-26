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

import com.zerocracy.Farm;
import com.zerocracy.Item;
import com.zerocracy.Project;
import com.zerocracy.Xocument;
import java.io.IOException;
import org.cactoos.time.DateAsText;
import org.xembly.Directives;

/**
 * Available vacancies.
 *
 * @author Kirill (g4s8.public@gmail.com)
 * @version $Id $
 * @since 0.22
 * @todo #925:30min Vacancies are not rendered on `/vacancies` page.
 *  This page should list all current vacancies in all projects.
 *  Let's display vacancy author, date and text.
 * @todo #925:30min Old vacancies are not removed automatically.
 *  Let's add new stakeholder which will remove vacancies from `vacancies.xml`
 *  which are  older than 32 days.
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
        try (final Item item = this.item()) {
            new Xocument(item.path()).modify(
                new Directives()
                    .xpath("/vacancies")
                    .add("vacancy")
                    .attr("project", project.pid())
                    .addIf("added")
                    .set(new DateAsText().asString())
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
     * Remove vacancy.
     * @param project Hiring project
     * @throws IOException If fails
     */
    public void remove(final Project project) throws IOException {
        try (final Item item = this.item()) {
            new Xocument(item.path()).modify(
                new Directives()
                    .xpath(
                        String.format(
                            "/vacancies/vacancy[@project='%s']",
                            project.pid()
                        )
                    ).remove()
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
     * The item.
     * @return Item
     * @throws IOException If fails
     */
    private Item item() throws IOException {
        return this.pmo.acq("vacancies.xml");
    }
}
