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
package com.zerocracy.farm;

import com.jcabi.log.Logger;
import com.jcabi.s3.Bucket;
import com.zerocracy.Farm;
import com.zerocracy.Project;
import com.zerocracy.farm.sync.Locks;
import com.zerocracy.pmo.Catalog;
import com.zerocracy.pmo.Pmo;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.EqualsAndHashCode;
import org.cactoos.iterable.Mapped;
import org.cactoos.list.SolidList;

/**
 * Farm in S3.
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@EqualsAndHashCode(of = "bucket")
public final class S3Farm implements Farm {

    /**
     * S3 bucket.
     */
    private final Bucket bucket;

    /**
     * Locks.
     */
    private final Locks locks;

    /**
     * Ctor.
     * @param bkt Bucket
     * @param locks Locks
     */
    public S3Farm(final Bucket bkt, final Locks locks) {
        this.bucket = bkt;
        this.locks = locks;
    }

    @Override
    public Iterable<Project> find(final String xpath) throws IOException {
        Iterable<Project> found;
        if ("@id='PMO'".equals(xpath)) {
            found = new SolidList<>(
                new S3Project(this.bucket, "PMO/")
            );
        } else {
            final ReadWriteLock rwl = this.locks.lock(
                new Pmo(this), "catalog.xml"
            );
            final Lock lock = rwl.writeLock();
            try {
                // @checkstyle MagicNumberCheck (1 line)
                if (!lock.tryLock(15L, TimeUnit.SECONDS)) {
                    throw new IOException("Failed to lock in 15 seconds");
                }
            } catch (final InterruptedException err) {
                throw new IllegalStateException("interrupted", err);
            }
            Logger.debug(this, "#find(): catalog.xml locked");
            try {
                final Catalog catalog = new Catalog(this).bootstrap();
                found = new Mapped<>(
                    prefix -> new S3Project(this.bucket, prefix),
                    catalog.findByXPath(xpath)
                );
                final boolean empty = !found.iterator().hasNext();
                Logger.debug(this, "#find(): empty?=%b", empty);
                if (empty) {
                    found = this.force(catalog, xpath);
                }
            } finally {
                lock.unlock();
                Logger.debug(this, "#find(): unlock");
            }
        }
        return found;
    }

    @Override
    public void close() {
        // nothing
    }

    /**
     * Delete all project files.
     * @param prefix The prefix
     * @throws IOException If fails
     */
    public void delete(final String prefix) throws IOException {
        for (final String ocket : this.bucket.list(prefix)) {
            this.bucket.remove(ocket);
        }
    }

    /**
     * Make sure it exists and return it.
     * @param catalog The catalog
     * @param xpath The XPath
     * @return List of found projects
     * @throws IOException If fails
     */
    private Iterable<Project> force(final Catalog catalog,
        final String xpath) throws IOException {
        final Matcher matcher = Pattern.compile(
            "\\s*@id\\s*=\\s*'([^']+)'\\s*"
        ).matcher(xpath);
        final Iterable<Project> found;
        if (matcher.matches()) {
            final String pid = matcher.group(1);
            Logger.debug(
                this, "#force(): adding new project: %s", pid
            );
            catalog.add(
                pid, String.format("%tY/%1$tm/%s/", new Date(), pid)
            );
            found = this.find(xpath);
        } else {
            found = Collections.emptyList();
        }
        return found;
    }
}
