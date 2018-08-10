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
import com.zerocracy.Xocument;
import java.io.IOException;
import org.cactoos.iterable.ItemAt;
import org.cactoos.iterable.Mapped;
import org.cactoos.scalar.IoCheckedScalar;
import org.cactoos.scalar.NumberOf;
import org.xembly.Directives;

/**
 * User options.
 *
 * @since 1.0
 */
public final class Options {
    /**
     * PMO.
     */
    private final Pmo pmo;
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
        this.pmo = pmo;
        this.uid = login;
    }

    /**
     * Bootstrap it.
     * @return Itself
     * @throws IOException If fails
     */
    public Options bootstrap() throws IOException {
        try (final Item item = this.item()) {
            new Xocument(item).bootstrap("pmo/options");
        }
        return this;
    }

    /**
     * Max jobs in agenda.
     * @return Jobs number
     * @throws IOException If fails
     */
    public int maxJobsInAgenda() throws IOException {
        try (final Item item = this.item()) {
            return new IoCheckedScalar<>(
                new ItemAt<Number>(
                    xpath -> Integer.MAX_VALUE,
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
     * Set max jobs in agenda.
     * @param max Max jobs in agenda
     * @throws IOException If fails
     */
    public void maxJobsInAgenda(final int max) throws IOException {
        try (final Item item = this.item()) {
            new Xocument(item.path()).modify(
                new Directives()
                    .xpath("/options")
                    .addIf("maxJobsInAgenda")
                    .set(max)
            );
        }
    }

    /**
     * Notify students option.
     * @return True if set
     * @throws IOException If fails
     */
    public boolean notifyStudents() throws IOException {
        return this.notify("students", true);
    }

    /**
     * Notify RFPS option.
     * @return True if set
     * @throws IOException If fails
     */
    public boolean notifyRfps() throws IOException {
        return this.notify("rfps", true);
    }

    /**
     * Notify publish option.
     * @return True if set
     * @throws IOException If fails
     */
    public boolean notifyPublish() throws IOException {
        return this.notify("publish", true);
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
        return this.pmo.acq(String.format("options/%s.xml", this.uid));
    }
}
