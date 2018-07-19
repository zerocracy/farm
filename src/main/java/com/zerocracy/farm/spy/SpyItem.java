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
package com.zerocracy.farm.spy;

import com.zerocracy.Item;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;
import lombok.EqualsAndHashCode;
import org.cactoos.Proc;
import org.cactoos.func.UncheckedProc;
import org.cactoos.text.TextOf;

/**
 * Spy {@link Item}.
 *
 * <p>There is no thread-safety guarantee.</p>
 *
 * @since 1.0
 */
@EqualsAndHashCode(of = "origin")
public final class SpyItem implements Item {

    /**
     * Origin.
     */
    private final Item origin;

    /**
     * Spy.
     */
    private final UncheckedProc<String> spy;

    /**
     * Start content.
     */
    private final AtomicReference<String> start;

    /**
     * Ctor.
     * @param itm The item
     * @param proc The spy
     */
    public SpyItem(final Item itm, final Proc<String> proc) {
        this.origin = itm;
        this.spy = new UncheckedProc<>(proc);
        this.start = new AtomicReference<>();
    }

    @Override
    public String toString() {
        return this.origin.toString();
    }

    @Override
    public Path path() throws IOException {
        final Path path = this.origin.path();
        if (path.toFile().exists()) {
            this.start.compareAndSet(null, new TextOf(path).asString());
        }
        this.spy.exec(String.format("path:%s", this.origin.toString()));
        return path;
    }

    @Override
    public void close() throws IOException {
        String before = this.start.get();
        if (before == null) {
            before = "";
        }
        final Path path = this.origin.path();
        String after = "";
        if (path.toFile().exists()) {
            after = new TextOf(path).asString();
        }
        if (!before.equals(after)) {
            this.spy.exec(
                String.format(
                    "update:%s:%d->%d",
                    this.origin.toString(),
                    before.length(), after.length()
                )
            );
        }
        this.spy.exec(String.format("close:%s", this.origin.toString()));
        this.origin.close();
    }

}
