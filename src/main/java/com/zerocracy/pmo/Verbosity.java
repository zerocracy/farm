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

import com.zerocracy.Farm;
import com.zerocracy.Item;
import com.zerocracy.Project;
import com.zerocracy.Xocument;
import java.io.IOException;
import java.time.Instant;
import org.cactoos.iterable.ItemAt;
import org.cactoos.iterable.Mapped;
import org.cactoos.scalar.IoCheckedScalar;
import org.cactoos.text.JoinedText;
import org.xembly.Directives;

/**
 * Verbosity of a user.
 *
 * @since 1.0
 */
public final class Verbosity {

    /**
     * Project.
     */
    private final Project pkt;

    /**
     * User.
     */
    private final String login;

    /**
     * Ctor.
     * @param farm Farm
     * @param user User
     */
    public Verbosity(final Farm farm, final String user) {
        this(new Pmo(farm), user);
    }

    /**
     * Ctor.
     * @param pmo Project
     * @param user User
     */
    public Verbosity(final Project pmo, final String user) {
        this.pkt = pmo;
        this.login = user;
    }

    /**
     * Add verbosity for a job.
     * @param project Project
     * @param job Job id
     * @param verbosity Messages for a job
     * @throws IOException If fails
     */
    public void add(final Project project, final String job,
        final int verbosity) throws IOException {
        this.add(project, job, verbosity, Instant.now());
    }

    /**
     * Add verbosity for a job.
     * @param project Project
     * @param job Job id
     * @param verbosity Messages for a job
     * @param time Added time
     * @throws IOException If fails
     * @checkstyle ParameterNumberCheck (5 lines)
     */
    public void add(final Project project, final String job,
        final int verbosity, final Instant time) throws IOException {
        try (final Item item = this.item()) {
            new Xocument(item).modify(
                new Directives()
                    .xpath("/verbosity")
                    .push()
                    .xpath(String.format("order[@job='%s']", job))
                    .remove()
                    .pop()
                    .add("order")
                    .attr("job", job)
                    .add("project")
                    .set(project.pid())
                    .up()
                    .add("messages")
                    .set(verbosity)
                    .up()
                    .add("added")
                    .set(time)
            );
        }
    }

    /**
     * Verbosity value for a job.
     * @return Message count
     * @throws IOException If fails
     */
    public int messages() throws IOException {
        try (final Item item = this.item()) {
            return new IoCheckedScalar<>(
                new ItemAt<Integer>(
                    iter -> 0,
                    new Mapped<>(
                        Integer::parseInt,
                        new Xocument(item)
                            .xpath("/verbosity/order/messages/text()")
                    )
                )
            ).value();
        }
    }

    /**
     * Remove all blanks older than specified date.
     * @param date Date
     * @throws IOException If failed
     */
    public void removeOlderThan(final Instant date) throws IOException {
        try (final Item item = this.item()) {
            new Xocument(item.path()).modify(
                new Directives()
                    .xpath(
                        new JoinedText(
                            "",
                            "/verbosity/order[xs:dateTime(added) < ",
                            "xs:dateTime('",
                            date.toString(),
                            "')]"
                        ).asString()
                    ).remove()
            );
        }
    }

    /**
     * Bootstrap it.
     * @return This
     * @throws IOException If fails
     */
    public Verbosity bootstrap() throws IOException {
        try (final Item item = this.item()) {
            new Xocument(item.path()).bootstrap("pmo/verbosity");
        }
        return this;
    }

    /**
     * The item.
     * @return Item
     * @throws IOException If fails
     */
    private Item item() throws IOException {
        return this.pkt.acq(
            String.format("verbosity/%s.xml", this.login)
        );
    }
}
