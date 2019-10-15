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

import com.zerocracy.Project;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import javax.sql.DataSource;

/**
 * Postgres locks.
 *
 * @since 1.0
 */
public final class PgLocks implements Locks {

    /**
     * Resource name.
     */
    private static final String RES = "ANY";

    /**
     * Data source.
     */
    private final DataSource data;

    /**
     * Thread holders for locks.
     */
    private final Map<String, PgLock.Holder> holders;

    /**
     * Ctor.
     *
     * @param data Data source
     */
    public PgLocks(final DataSource data) {
        this.data = data;
        this.holders = new ConcurrentHashMap<>();
    }

    @Override
    public Lock lock(final Project pkt, final String res) throws IOException {
        final String lid = String.format("%s:%s", pkt.pid(), PgLocks.RES);
        final PgLock.Holder holder = this.holders
            .computeIfAbsent(lid, key -> new PgLock.Holder());
        return new PgLock(this.data, pkt.pid(), PgLocks.RES, holder);
    }
}
