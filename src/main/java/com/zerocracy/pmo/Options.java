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

import com.zerocracy.ItemXml;
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
     */
    public Options bootstrap() {
        return this;
    }

    /**
     * Max jobs in agenda.
     * @return Jobs number
     * @throws IOException If fails
     */
    public int maxJobsInAgenda() throws IOException {
        return new IoCheckedScalar<>(
            new ItemAt<Number>(
                xpath -> Integer.MAX_VALUE,
                new Mapped<>(
                    NumberOf::new,
                    this.item().xpath("/options/maxJobsInAgenda/text()")
                )
            )
        ).value().intValue();
    }

    /**
     * Set max jobs in agenda.
     * @param max Max jobs in agenda
     * @throws IOException If fails
     */
    @SuppressWarnings("PMD.AvoidDuplicateLiterals")
    public void maxJobsInAgenda(final int max) throws IOException {
        this.item().update(
            new Directives()
                .xpath("/options")
                .addIf("maxJobsInAgenda")
                .set(max)
        );
    }

    /**
     * Max REV jobs in agenda.
     * @return REV jobs number
     * @throws IOException If fails
     */
    public int maxRevJobsInAgenda() throws IOException {
        return new IoCheckedScalar<>(
            new ItemAt<Number>(
                xpath -> Integer.MAX_VALUE,
                new Mapped<>(
                    NumberOf::new,
                    this.item()
                        .xpath("/options/maxRevJobsInAgenda/text()")
                )
            )
        ).value().intValue();
    }

    /**
     * Set max REV jobs in agenda.
     * @param max Max REV jobs in agenda
     * @throws IOException If fails
     */
    @SuppressWarnings("PMD.AvoidDuplicateLiterals")
    public void maxRevJobsInAgenda(final int max) throws IOException {
        this.item().update(
            new Directives()
                .xpath("/options")
                .addIf("maxRevJobsInAgenda")
                .set(max)
        );
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
        return new IoCheckedScalar<>(
            new ItemAt<Boolean>(
                xpath -> def,
                new Mapped<>(
                    Boolean::valueOf,
                    this.item().xpath(
                        String.format("/options/notify/%s/text()", name)
                    )
                )
            )
        ).value();
    }

    /**
     * The item.
     * @return Item
     * @throws IOException If fails
     */
    private ItemXml item() throws IOException {
        return new ItemXml(
            this.pmo.acq(String.format("options/%s.xml", this.uid)),
            "pmo/options"
        );
    }
}
