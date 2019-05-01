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
package com.zerocracy.pm;

import com.jcabi.aspects.Tv;
import com.zerocracy.Farm;
import com.zerocracy.Item;
import com.zerocracy.Policy;
import com.zerocracy.Project;
import com.zerocracy.Xocument;
import java.io.IOException;
import org.cactoos.iterable.ItemAt;
import org.cactoos.iterable.Mapped;
import org.cactoos.scalar.IoCheckedScalar;
import org.cactoos.scalar.NumberOf;
import org.xembly.Directives;

/**
 * Project options.
 *
 * @since 1.0
 */
public final class PktOptions {

    /**
     * Project.
     */
    private final Project pkt;

    /**
     * Farm.
     */
    private final Farm farm;

    /**
     * Ctor.
     * @param pkt Project
     * @param farm Farm
     */
    public PktOptions(final Project pkt, final Farm farm) {
        this.pkt = pkt;
        this.farm = farm;
    }

    /**
     * Bootstrap it.
     * @return Itself
     * @throws IOException If fails
     */
    public PktOptions bootstrap() throws IOException {
        try (final Item team = this.item()) {
            new Xocument(team).bootstrap("pm/options");
        }
        return this;
    }

    /**
     * Days to close task option.
     * @return Option value
     * @throws IOException If fails
     */
    public int daysToCloseTask() throws IOException {
        try (final Item item = this.item()) {
            return new IoCheckedScalar<>(
                new ItemAt<Number>(
                    xpath -> new Policy(this.farm).get("8.days", Tv.TEN),
                    new Mapped<>(
                        NumberOf::new,
                        new Xocument(item.path())
                            .xpath("/options/daysToCloseTask/text()")
                    )
                )
            ).value().intValue();
        }
    }

    /**
     * Change daysToCloseTask option.
     * @param val New option value
     * @throws IOException If fails
     */
    public void daysToCloseTask(final int val) throws IOException {
        try (final Item item = this.item()) {
            new Xocument(item).modify(
                new Directives()
                    .xpath("/options")
                    .addIf("daysToCloseTask")
                    .set(val)
            );
        }
    }

    /**
     * The item.
     * @return Item
     * @throws IOException If fails
     */
    private Item item() throws IOException {
        return this.pkt.acq("options.xml");
    }
}
