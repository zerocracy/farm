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
package com.zerocracy;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import org.cactoos.scalar.And;
import org.cactoos.scalar.IoCheckedScalar;

/**
 * Project's items transaction.
 *
 * @since 1.0
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

    @SuppressWarnings(
        {
            "IOResourceOpenedButNotSafelyClosed",
            "NestedIfDepthCheck"
        }
        )
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
                    item = this.item(file);
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
        new IoCheckedScalar<>(
            new And(Txn.TxnItem::commit, this.items.values())
        ).value();
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
     * Create new item.
     * @param file Source file.
     * @return New TxnItem
     * @throws IOException If fails
     */
    private Txn.TxnItem item(final String file) throws IOException {
        final File tmp = File.createTempFile("txn_", ".tmp");
        final Item src = this.origin.acq(file);
        if (src.path().toFile().exists()) {
            Files.copy(
                src.path(),
                tmp.toPath(),
                StandardCopyOption.REPLACE_EXISTING
            );
        }
        return new Txn.TxnItem(src, tmp);
    }

    /**
     * Transaction item.
     */
    private static final class TxnItem implements Item {
        /**
         * Origin item.
         */
        private final Item origin;
        /**
         * Temporary file.
         */
        private final File tmp;
        /**
         * Committed flag.
         */
        private final AtomicBoolean commited;

        /**
         * Ctor.
         * @param origin Origin item
         * @param tmp Temp file
         */
        private TxnItem(final Item origin, final File tmp) {
            this.origin = origin;
            this.tmp = tmp;
            this.commited = new AtomicBoolean();
        }

        @Override
        public Path path() {
            return this.tmp.toPath();
        }

        @Override
        public void close() throws IOException {
            this.close(false);
        }

        /**
         * Commit changes for single item.
         * @throws IOException If fails
         */
        public void commit() throws IOException {
            if (!this.commited.compareAndSet(false, true)) {
                throw new IOException("This item was committed");
            }
        }

        /**
         * Close this item with origin if force.
         * @param force True if close origin item
         * @throws IOException If fails
         */
        public void close(final boolean force) throws IOException {
            if (this.commited.get()) {
                Files.move(
                    this.tmp.toPath(),
                    this.origin.path(),
                    StandardCopyOption.REPLACE_EXISTING
                );
            }
            if (force) {
                this.origin.close();
            }
            if (force && this.tmp.exists() && !this.tmp.delete()) {
                throw new IOException("Failed to delete tmp file");
            }
        }
    }
}
