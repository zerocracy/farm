/**
 * Copyright (c) 2016-2017 Zerocracy
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

import com.jcabi.aspects.Cacheable;
import com.jcabi.s3.Bucket;
import com.zerocracy.jstk.Farm;
import com.zerocracy.jstk.Project;
import com.zerocracy.pmo.Catalog;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.EqualsAndHashCode;
import org.cactoos.iterable.ListOf;
import org.cactoos.iterable.Mapped;

/**
 * Farm in S3.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.1
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@EqualsAndHashCode(of = "bucket")
public final class S3Farm implements Farm {

    /**
     * S3 bucket.
     */
    private final Bucket bucket;

    /**
     * Path to temporary storage.
     */
    private final Path temp;

    /**
     * Ctor.
     * @param bkt Bucket
     * @throws IOException If fails
     */
    public S3Farm(final Bucket bkt) throws IOException {
        this(bkt, Files.createTempDirectory(""));
    }

    /**
     * Ctor.
     * @param bkt Bucket
     * @param tmp Temporary storage
     */
    public S3Farm(final Bucket bkt, final Path tmp) {
        this.bucket = bkt;
        this.temp = tmp;
    }

    @Override
    @Cacheable(lifetime = 2, unit = TimeUnit.SECONDS)
    public Iterable<Project> find(final String xpath) throws IOException {
        Iterable<Project> found;
        if ("@id='PMO'".equals(xpath)) {
            found = new ListOf<>(
                new S3Project(this.bucket, "PMO/", this.temp)
            );
        } else {
            final Catalog catalog = new Catalog(this).bootstrap();
            found = new Mapped<>(
                catalog.findByXPath(xpath),
                prefix -> new S3Project(this.bucket, prefix, this.temp)
            );
            if (!found.iterator().hasNext()) {
                found = this.force(catalog, xpath);
            }
        }
        return found;
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
