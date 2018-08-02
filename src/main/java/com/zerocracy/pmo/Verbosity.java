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

import com.zerocracy.Item;
import com.zerocracy.Project;
import com.zerocracy.Xocument;
import java.io.IOException;
import java.time.Instant;
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
    private final Pmo pkt;
    /**
     * User.
     */
    private final String login;
    /**
     * Ctor.
     * @param pmo Project
     * @param user User
     */
    public Verbosity(final Pmo pmo, final String user) {
        this.pkt = pmo;
        this.login = user;
    }

    /**
     * Add verbosity for a job.
     * @param job Job id
     * @param project Project
     * @param verbosity Messages for a job
     * @throws IOException If fails
     * @todo #1233:30min Verbosity.add is throwing an exception when trying
     *  to add verbosity to a job more than once. We should check verbosity
     *  before adding a new element and override the previous value to
     *  prevent this exception being thrown. After the fix, test the code
     *  removing ignore tag from VerbositTest.overrideVerbosity.
     */
    public void add(final String job, final Project project,
        final int verbosity) throws IOException {
        try (final Item item = this.item()) {
            new Xocument(item).modify(
                new Directives()
                    .xpath("/verbosity")
                    .add("order")
                    .attr("job", job)
                    .add("project")
                    .set(project.pid())
                    .up()
                    .add("messages")
                    .set(verbosity)
                    .up()
                    .add("added")
                    .set(Instant.now())
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
