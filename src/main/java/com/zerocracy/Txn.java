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
package com.zerocracy;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import org.cactoos.scalar.And;
import org.cactoos.scalar.IoCheckedScalar;

/**
 * Project's items transaction.
 *
 * @author Kirill (g4s8.public@gmail.com)
 * @version $Id$
 * @since 0.18.8
 */
public final class Txn implements Project, Closeable {
    /**
     * Origin project.
     */
    private final Project origin;
    /**
     * Committed flag.
     */
    private final AtomicBoolean committed;
    /**
     * Acquired items.
     */
    private final Map<String, Txn.TxnItem> items;

    /**
     * Ctor.
     * @param origin Origin project
     */
    public Txn(final Project origin) {
        this.origin = origin;
        this.committed = new AtomicBoolean();
        this.items = new HashMap<>(1);
    }

    @SuppressWarnings("IOResourceOpenedButNotSafelyClosed")
    @Override
    public Item acq(final String file) throws IOException {
        final Txn.TxnItem item;
        if (this.items.containsKey(file)) {
            item = this.items.get(file);
        } else {
            synchronized (this.items) {
                if (this.items.containsKey(file)) {
                    item = this.items.get(file);
                } else {
                    item = new Txn.TxnItem(this.origin.acq(file));
                    this.items.put(file, item);
                }
            }
        }
        return item;
    }

    @Override
    public String pid() throws IOException {
        return this.origin.pid();
    }

    /**
     * Commit transaction.
     * @throws IOException If fails
     */
    public void commit() throws IOException {
        if (!this.committed.compareAndSet(false, true)) {
            throw new IOException("Already committed");
        }
    }

    @Override
    public void close() throws IOException {
        new IoCheckedScalar<>(
            new And(
                (Txn.TxnItem item) -> item.close(true),
                this.items.values()
            )
        ).value();
    }

    /**
     * Transaction item.
     * @todo #409:30min Txn.Item is not implemented.
     *  This class should keep local changes for single item
     *  and push them to origin item on commit. Also it should revert
     *  all local changes when transaction closed without commit() call.
     */
    private static final class TxnItem implements Item {
        /**
         * Origin item.
         */
        private final Item origin;

        /**
         * Ctor.
         * @param origin Origin item
         */
        private TxnItem(final Item origin) {
            this.origin = origin;
        }

        @Override
        public Path path() throws IOException {
            return this.origin.path();
        }

        @Override
        public void close() throws IOException {
            this.close(false);
        }

        /**
         * Close this item with origin if force.
         * @param force True if close origin item
         * @throws IOException If fails
         */
        public void close(final boolean force) throws IOException {
            if (force) {
                this.origin.close();
            }
        }
    }
}
