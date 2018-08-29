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
package com.zerocracy.farm.sync;

import com.mongodb.MongoClient;
import com.mongodb.client.model.Filters;
import com.zerocracy.Farm;
import com.zerocracy.entry.ExtMongo;
import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import org.bson.Document;

/**
 * {@link Lock} using MongoDB.
 *
 * @since 1.0
 */
public final class MongoDbLock implements Lock {

    /**
     * Mongo database name.
     */
    private static final String DATABASE = "locksdb";

    /**
     * Mongo lock collection name.
     */
    private static final String COLLECTION = "locks";

    /**
     * Resource field name.
     */
    private static final String RESOURCE = "resource";

    /**
     * Time field name.
     */
    private static final String TIME = "time";

    /**
     * MongoDB client.
     */
    private final MongoClient client;

    /**
     * Resource to be locked.
     */
    private final String resource;

    /**
     * Constructor.
     * @param farm Farm containing the resource
     * @param resource Resource to be locked
     * @throws IOException If something goes wrong
     */
    MongoDbLock(final Farm farm, final String resource) throws IOException {
        this.client = new ExtMongo(farm).value();
        this.resource = resource;
    }

    @Override
    public StackTraceElement[] stacktrace() {
        throw new UnsupportedOperationException(
            "StackTraceElement[] is not implemented"
        );
    }

    // @todo #1644:30min Implement external locking mechanism based in MongoDB.
    //  According to javas Lock interface, when there is already a lock on given
    //  resource, it should suspend the thread until it is possible to do a
    //  lock. This class is not implementing this behavior on lock() method.
    //  Implement this so this lock should block until the resource is unlocked.
    @Override
    public void lock() {
        final Document lock = new Document();
        lock.put(MongoDbLock.RESOURCE, this.resource);
        lock.put(MongoDbLock.TIME, Instant.now());
        this.client.getDatabase(
            MongoDbLock.DATABASE
        ).getCollection(MongoDbLock.COLLECTION).insertOne(lock);
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        throw new UnsupportedOperationException(
            "lockInterruptibly() is not implemented"
        );
    }

    @Override
    public boolean tryLock() {
        final boolean lock;
        if (this.locked()) {
            lock = false;
        } else {
            this.lock();
            lock = true;
        }
        return lock;
    }

    @Override
    public boolean tryLock(final long time, final TimeUnit unit)
        throws InterruptedException {
        throw new UnsupportedOperationException(
            "tryLock(long time, TimeUnit unit) is not implemented"
        );
    }

    @Override
    public void unlock() {
        this.client.getDatabase(
            MongoDbLock.DATABASE
        ).getCollection(MongoDbLock.COLLECTION).deleteOne(
            Filters.eq(MongoDbLock.RESOURCE, this.resource)
            );
    }

    @Override
    public Condition newCondition() {
        throw new UnsupportedOperationException(
            "newCondition() is not implemented"
        );
    }

    @Override
    public String toString() {
        final String text;
        if (this.locked()) {
            text = "locked";
        } else {
            text = "free";
        }
        return text;
    }

    /**
     * Is this lock locked?
     * @return The lock locked status
     */
    private boolean locked() {
        return this.client.getDatabase(
            MongoDbLock.DATABASE
        ).getCollection(MongoDbLock.COLLECTION).find(
            Filters.eq(MongoDbLock.RESOURCE, this.resource)
        ).iterator().hasNext();
    }
}
