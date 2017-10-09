/**
 * Copyright (c) 2016-2017 Zerocracy
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

import com.zerocracy.Xocument;
import com.zerocracy.jstk.Item;
import com.zerocracy.jstk.Project;
import java.io.IOException;
import org.xembly.Directives;

/**
 * Speed of delivery.
 * @author Kirill (g4s8.public@gmail.com)
 * @version $Id$
 * @since 0.17
 * @todo #250:30m Add new voter for `speed.xml`
 *  This voter should pay attention to user's speed from `speed.xml`
 *  and vote for that person who is the fastest.
 */
public final class Speed {

    /**
     * Project.
     */
    private final Project pmo;

    /**
     * Login of the person.
     */
    private final String login;

    /**
     * Ctor.
     * @param pkt Project
     * @param user The user
     */
    public Speed(final Project pkt, final String user) {
        this.pmo = pkt;
        this.login = user;
    }

    /**
     * Bootstrap it.
     * @return This
     * @throws IOException If fails
     */
    public Speed bootstrap() throws IOException {
        try (final Item item = this.item()) {
            new Xocument(item.path()).bootstrap("pmo/speed");
        }
        return this;
    }

    /**
     * Add points to the list.
     * @param project A project
     * @param job A job
     * @param minutes How many minutes was spent on this job
     * @throws IOException If fails
     */
    public void add(final String project, final String job, final long minutes)
        throws IOException {
        try (final Item item = this.item()) {
            new Xocument(item.path()).modify(
                new Directives()
                    .xpath("/speed")
                    .add("order")
                    .attr("job", job)
                    .add("project")
                    .set(project)
                    .up()
                    .add("minutes")
                    .set(minutes)
                    .up()
                    .up()
            );
        }
    }

    /**
     * Return full list of jobs.
     * @return List of job IDs
     * @throws IOException If fails
     */
    public Iterable<String> jobs() throws IOException {
        try (final Item item = this.item()) {
            return new Xocument(item.path()).xpath(
                "/speed/order/@job"
            );
        }
    }

    /**
     * The item.
     * @return Item
     * @throws IOException If fails
     */
    private Item item() throws IOException {
        return this.pmo.acq(
            String.format("speed/%s.xml", this.login)
        );
    }
}
