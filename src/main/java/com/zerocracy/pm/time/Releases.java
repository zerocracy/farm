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
package com.zerocracy.pm.time;

import com.zerocracy.Item;
import com.zerocracy.Project;
import com.zerocracy.Xocument;
import java.io.IOException;
import java.time.Instant;
import java.util.Comparator;
import org.cactoos.iterable.ItemAt;
import org.cactoos.iterable.Mapped;
import org.cactoos.iterable.Sorted;
import org.cactoos.scalar.IoCheckedScalar;
import org.xembly.Directives;

/**
 * Project releases for each repository.
 *
 * @since 0.25
 */
public final class Releases {

    /**
     * Project.
     */
    private final Project pkt;

    /**
     * Ctor.
     *
     * @param project Project
     */
    public Releases(final Project project) {
        this.pkt = project;
    }

    /**
     * Latest release.
     *
     * @return Publish time
     * @throws IOException If fails
     */
    public Instant latest() throws IOException {
        try (final Item item = this.item()) {
            return new IoCheckedScalar<>(
                new ItemAt<>(
                    Instant.ofEpochMilli(0L),
                    new Sorted<>(
                        Comparator.reverseOrder(),
                        new Mapped<>(
                            Instant::parse,
                            new Xocument(item.path())
                                .xpath("/releases/release/published/text()")
                        )
                    )
                )
            ).value();
        }
    }

    /**
     * Add new release.
     *
     * @param repo Repository coordinates
     * @param tag Release tag
     * @param time Publish time
     * @throws IOException If fails
     */
    public void add(final String repo, final String tag, final Instant time)
        throws IOException {
        try (final Item item = this.item()) {
            new Xocument(item.path()).modify(
                new Directives()
                    .xpath("/releases")
                    .add("release")
                    .attr("repo", repo)
                    .attr("tag", tag)
                    .add("published")
                    .set(time.toString())
            );
        }
    }

    /**
     * Bootstrap it.
     *
     * @return Itself
     * @throws IOException If fails
     */
    public Releases bootstrap() throws IOException {
        try (final Item item = this.item()) {
            new Xocument(item.path()).bootstrap("pm/time/releases");
        }
        return this;
    }

    /**
     * The item.
     *
     * @return Item
     * @throws IOException If fails
     */
    private Item item() throws IOException {
        return this.pkt.acq("releases.xml");
    }
}
