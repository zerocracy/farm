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
import com.zerocracy.Project;
import com.zerocracy.Xocument;
import java.io.IOException;
import java.time.Instant;
import org.xembly.Directives;

/**
 * Negligence. This metric calculates how many times a user has lost
 * a job due to missing the deadline (too many days passed without a reason for
 * waiting).
 *
 * @since 1.0
 * @todo #1141:30min Use this metric inside resing_on_delay.groovy. After this,
 *  finish implementing the voter VsNegligence: similar to VsSpeed and
 *  VsWorkload, it should vote for the user with the highest negligence
 *  (most number of delays). When done, the voter should be declared in
 *  elect_performer.groovy with weight -1 (since the highest negligence will
 *  have the highest vote).
 */
public final class Negligence {

    /**
     * Project.
     */
    private final Pmo pmo;

    /**
     * Login of the person.
     */
    private final String login;

    /**
     * Ctor.
     * @param farm The farm
     * @param user The user
     */
    public Negligence(final Farm farm, final String user) {
        this(new Pmo(farm), user);
    }

    /**
     * Ctor.
     * @param pkt Pmo project
     * @param user The user
     */
    public Negligence(final Pmo pkt, final String user) {
        this.pmo = pkt;
        this.login = user;
    }

    /**
     * How many times did the user lost tasks due to negligence?
     * @return Integer number of delays
     * @throws IOException If fails
     */
    public int delays() throws IOException {
        try (final Item item = this.item()) {
            return new Xocument(item.path())
                .nodes("/negligence/order")
                .size();
        }
    }

    /**
     * Add a negligence.
     * @param proj Project
     * @param job Job id
     * @throws IOException If fails
     */
    public void add(final Project proj, final String job) throws IOException {
        try (final Item item = this.item()) {
            new Xocument(item.path()).modify(
                new Directives()
                    .xpath("/negligence")
                    .add("order")
                    .attr("job", job)
                    .add("project")
                    .set(proj.pid())
                    .up()
                    .add("added")
                    .set(Instant.now().toString())
            );
        }
    }

    /**
     * Bootstrap it.
     * @return This
     * @throws IOException If fails
     */
    public Negligence bootstrap() throws IOException {
        try (final Item item = this.item()) {
            new Xocument(item.path()).bootstrap("pmo/negligence");
        }
        return this;
    }

    /**
     * The item.
     * @return Item
     * @throws IOException If fails
     */
    private Item item() throws IOException {
        return this.pmo.acq(
            String.format("negligence/%s.xml", this.login)
        );
    }
}
