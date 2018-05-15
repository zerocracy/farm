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

import com.zerocracy.Item;
import com.zerocracy.Xocument;
import java.io.IOException;
import org.cactoos.iterable.ItemAt;
import org.cactoos.iterable.Mapped;
import org.cactoos.scalar.IoCheckedScalar;
import org.cactoos.scalar.NumberOf;

/**
 * User options.
 *
 * @author Kirill (g4s8.public@gmail.com)
 * @version $Id $
 * @since 0.22
 * @todo #703:30min Options are not used. Election should check
 *  maxJobsInAgenda option, notify stakeholders should check notify options:
 *  notifyPublish, notifyRfps, notifyStudents. Also user should be able to
 *  download, edit and upload options.xml file on profile page.
 */
public final class Options {
    /**
     * PMO.
     */
    private final Pmo pkt;
    /**
     * User id.
     */
    private final String uid;
    /**
     * Ctor.
     * @param pmo PMO project
     * @param login User id
     */
    public Options(final Pmo pmo, final String login) {
        this.pkt = pmo;
        this.uid = login;
    }

    /**
     * Bootstrap it.
     * @return Itself
     * @throws IOException If fails
     */
    public Options bootstrap() throws IOException {
        try (final Item team = this.item()) {
            new Xocument(team).bootstrap("pmo/options");
        }
        return this;
    }

    /**
     * Max jobs in agenda or default value.
     * @param def Default value
     * @return Jobs number
     * @throws IOException If fails
     */
    public int maxJobsInAgenda(final int def) throws IOException {
        try (final Item item = this.item()) {
            return new IoCheckedScalar<>(
                new ItemAt<Number>(
                    xpath -> def,
                    new Mapped<>(
                        NumberOf::new,
                        new Xocument(item.path())
                            .xpath("/options/maxJobsInAgenda/text()")
                    )
                )
            ).value().intValue();
        }
    }

    /**
     * Notify students option.
     * @param def Default value
     * @return True if set
     * @throws IOException If fails
     */
    public boolean notifyStudents(final boolean def) throws IOException {
        return this.notify("students", def);
    }

    /**
     * Notify RFPS option.
     * @param def Default value
     * @return True if set
     * @throws IOException If fails
     */
    public boolean notifyRfps(final boolean def) throws IOException {
        return this.notify("rfps", def);
    }

    /**
     * Notify publish option.
     * @param def Default value
     * @return True if set
     * @throws IOException If fails
     */
    public boolean notifyPublish(final boolean def) throws IOException {
        return this.notify("publish", def);
    }

    /**
     * Check notify option.
     * @param name Option name
     * @param def Default value
     * @return True if set
     * @throws IOException If fails
     */
    private boolean notify(final String name, final boolean def)
        throws IOException {
        try (final Item item = this.item()) {
            return new IoCheckedScalar<>(
                new ItemAt<Boolean>(
                    xpath -> def,
                    new Mapped<>(
                        Boolean::valueOf,
                        new Xocument(item.path())
                            .xpath(
                                String.format(
                                    "/options/notify/%s/text()",
                                    name
                                )
                            )
                    )
                )
            ).value();
        }
    }

    /**
     * The item.
     * @return Item
     * @throws IOException If fails
     */
    private Item item() throws IOException {
        return this.pkt.acq(String.format("options/%s.xml", this.uid));
    }
}
