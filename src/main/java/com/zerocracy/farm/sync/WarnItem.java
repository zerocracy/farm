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
package com.zerocracy.farm.sync;

import com.jcabi.aspects.Tv;
import com.jcabi.log.Logger;
import com.zerocracy.Item;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import org.cactoos.Func;
import org.cactoos.Proc;

/**
 * Log warning if item was opened too long.
 *
 * @since 1.0
 */
public final class WarnItem implements Item {

    /**
     * Open duration to warn.
     */
    private static final long DUR_WARN =
        TimeUnit.SECONDS.toNanos((long) Tv.THIRTY);

    /**
     * Item name to log.
     */
    private final String name;

    /**
     * Origin item.
     */
    private final Item origin;

    /**
     * Ctor.
     * @param name Item name to log
     * @param origin Origin item to wrap
     */
    public WarnItem(final String name, final Item origin) {
        this.name = name;
        this.origin = origin;
    }

    @Override
    public <T> T read(final Func<Path, T> reader) throws IOException {
        final long opened = System.nanoTime();
        final T res = this.origin.read(reader);
        this.warn(opened);
        return res;
    }

    @Override
    public void update(final Proc<Path> writer) throws IOException {
        final long opened = System.nanoTime();
        this.origin.update(writer);
        this.warn(opened);
    }

    /**
     * Warn if necessary.
     * @param opened Opened time
     */
    private void warn(final long opened) {
        final long dur = System.nanoTime() - opened;
        if (dur >= WarnItem.DUR_WARN) {
            Logger.warn(
                this,
                String.join(
                    "; ",
                    "Item '%s' was opened too long: %[nano]s;"
                ),
                this.name, dur
            );
        }
    }
}
