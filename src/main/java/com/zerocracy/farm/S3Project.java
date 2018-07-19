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
package com.zerocracy.farm;

import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.jcabi.s3.Bucket;
import com.zerocracy.Item;
import com.zerocracy.Project;
import com.zerocracy.farm.fake.FkItem;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;
import org.cactoos.collection.Joined;
import org.cactoos.collection.Mapped;
import org.cactoos.time.DateAsText;
import org.xembly.Directive;
import org.xembly.Directives;
import org.xembly.Xembler;

/**
 * Project in S3.
 *
 * @since 1.0
 */
@EqualsAndHashCode(of = "prefix")
final class S3Project implements Project {

    /**
     * S3 bucket.
     */
    private final Bucket bucket;

    /**
     * Path in the bucket.
     */
    private final String prefix;

    /**
     * Path to temporary storage.
     */
    private final Path temp;

    /**
     * Ctor.
     * @param bkt Bucket
     * @param pfx Prefix
     * @throws IOException If fails
     */
    S3Project(final Bucket bkt, final String pfx) throws IOException {
        this(bkt, pfx, Files.createTempDirectory(""));
    }

    /**
     * Ctor.
     * @param bkt Bucket
     * @param pfx Prefix
     * @param tmp Storage
     */
    S3Project(final Bucket bkt, final String pfx, final Path tmp) {
        this.bucket = bkt;
        this.prefix = pfx;
        this.temp = tmp;
    }

    @Override
    public String pid() {
        String name = StringUtils.stripEnd(this.prefix, "/");
        if (name.contains("/")) {
            name = StringUtils.substringAfterLast(name, "/");
        }
        return name;
    }

    @Override
    public Item acq(final String file) throws IOException {
        final Item item;
        if ("_list.xml".equals(file)) {
            final ObjectListing listing = this.bucket.region().aws()
                .listObjects(this.bucket.name(), this.prefix);
            item = new FkItem(
                new Xembler(
                    new Directives().add("items").append(
                        new Joined<Directive>(
                            new Mapped<S3ObjectSummary, Iterable<Directive>>(
                                sum -> new Directives()
                                    .add("item")
                                    .add("name")
                                    .set(
                                        sum.getKey().substring(
                                            this.prefix.length()
                                        )
                                    )
                                    .up()
                                    .add("size").set(sum.getSize()).up()
                                    .add("modified")
                                    .set(
                                        new DateAsText(
                                            sum.getLastModified()
                                        ).asString()
                                    )
                                    .up()
                                    .up(),
                                listing.getObjectSummaries()
                            )
                        )
                    )
                ).xmlQuietly()
            );
        } else {
            if (!file.matches("[a-z0-9\\-/]+\\.[a-z]+")) {
                throw new IllegalArgumentException(
                    String.format(
                        "Unacceptable file name: \"%s\"", file
                    )
                );
            }
            final String key = String.format("%s%s", this.prefix, file);
            item = new S3Item(
                this.bucket.ocket(key),
                this.temp.resolve(key)
            );
        }
        return item;
    }

}
