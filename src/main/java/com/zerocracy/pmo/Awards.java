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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import org.cactoos.time.DateAsText;
import org.xembly.Directives;

/**
 * Awards of one person.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.12
 */
public final class Awards {

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
    public Awards(final Project pkt, final String user) {
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
     * Add points to the list.
     * @param points How many points
     * @param job Job ID
     * @param reason The reason
     * @param added When they were added
     * @throws IOException If fails
     * @checkstyle ParameterNumberCheck (5 lines)
     */
    public void add(final int points, final String job, final String reason,
        final LocalDateTime added)
        throws IOException {
        final Date date = Date.from(added.atZone(ZoneId.of("UTC")).toInstant());
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
                    .set(this.pmo.pid())
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
                    String.format(
                        "sum(/awards/award[added > '%s']/points/text())",
                        new DateAsText(
                            // @checkstyle MagicNumberCheck (1 line)
                            LocalDateTime.now().minusDays(90),
                            "yyyy-MM-dd"
                        ).asString()
                    )
                ).get(0)
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
