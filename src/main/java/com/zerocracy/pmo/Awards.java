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
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import org.cactoos.list.Mapped;
import org.cactoos.text.JoinedText;
import org.cactoos.time.DateAsText;
import org.xembly.Directives;

/**
 * Awards of one person.
 *
 * @since 1.0
 */
public final class Awards {

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
    public Awards(final Farm farm, final String user) {
        this(new Pmo(farm), user);
    }

    /**
     * Ctor.
     * @param pkt Project
     * @param user The user
     */
    public Awards(final Pmo pkt, final String user) {
        this.pmo = pkt;
        this.login = user;
    }

    /**
     * Bootstrap it.
     * @return This
     * @throws IOException If fails
     */
    public Awards bootstrap() throws IOException {
        try (final Item item = this.item()) {
            new Xocument(item.path()).bootstrap("pmo/awards");
        }
        return this;
    }

    /**
     * Remove all awards older than specified date.
     * @param date Date
     * @throws IOException If failed
     */
    public void removeOlderThan(final Date date) throws IOException {
        try (final Item item = this.item()) {
            new Xocument(item.path()).modify(
                new Directives()
                    .xpath(
                        new JoinedText(
                            "",
                            "/awards/award[xs:dateTime(added) < ",
                            "xs:dateTime('",
                            new DateAsText(date).asString(),
                            "')]"
                        ).asString()
                    ).remove()
            );
        }
    }

    /**
     * Add points to the list.
     * @param project The project
     * @param points How many points
     * @param job Job ID
     * @param reason The reason
     * @throws IOException If fails
     * @checkstyle ParameterNumberCheck (5 lines)
     */
    public void add(final Project project, final int points,
        final String job, final String reason)
        throws IOException {
        this.add(project, points, job, reason, new Date());
    }

    /**
     * Add points to the list.
     * @param project The project
     * @param points How many points
     * @param job Job ID
     * @param reason The reason
     * @param date Award created date
     * @throws IOException If fails
     * @checkstyle ParameterNumberCheck (5 lines)
     */
    public void add(final Project project, final int points,
        final String job, final String reason, final Date date)
        throws IOException {
        if (points == 0) {
            throw new IllegalArgumentException(
                String.format(
                    "Points can't be zero for %s: %s",
                    job, reason
                )
            );
        }
        try (final Item item = this.item()) {
            new Xocument(item.path()).modify(
                new Directives()
                    .xpath("/awards")
                    .add("award")
                    .add("points")
                    .set(points)
                    .up()
                    .add("added").set(new DateAsText(date).asString()).up()
                    .add("project")
                    .set(project.pid())
                    .up()
                    .add("reason")
                    .set(reason)
                    .up()
                    .add("job")
                    .set(job)
            );
        }
    }

    /**
     * Total points.
     * @return Points
     * @throws IOException If fails
     */
    public int total() throws IOException {
        try (final Item item = this.item()) {
            return Integer.parseInt(
                new Xocument(item.path()).xpath(
                    "sum(/awards/award/points/text())"
                ).get(0)
            );
        }
    }

    /**
     * Rewards for the last days.
     * @param days Number of last days to check
     * @return List of awards
     * @throws IOException If fails
     */
    public List<Integer> awards(final int days) throws IOException {
        try (final Item item = this.item()) {
            return new Mapped<>(
                Integer::parseInt,
                new Xocument(item.path()).xpath(
                    new JoinedText(
                        "",
                        "/awards/award[",
                        "xs:dateTime(added) > xs:dateTime('",
                        new DateAsText(
                            ZonedDateTime.now()
                                .minusDays(days)
                                .toInstant()
                                .toEpochMilli()
                        ).asString(),
                        "')]/points/text()"
                    ).asString()
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
        return this.pmo.acq(
            String.format("awards/%s.xml", this.login)
        );
    }

}
