/**
 * Copyright (c) 2016 Zerocracy
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

import com.jcabi.s3.Bucket;
import com.zerocracy.jstk.Farm;
import com.zerocracy.jstk.Project;
import com.zerocracy.jstk.Stakeholder;
import com.zerocracy.pmo.Catalog;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Farm in S3.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.1
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
final class S3Farm implements Farm {

    /**
     * Query pattern.
     */
    private static final Pattern QUERY = Pattern.compile(
        "|\\s*([a-z.]+)\\s*=\\s*([^\\s]+)\\s*"
    );

    /**
     * S3 bucket.
     */
    private final Bucket bucket;

    /**
     * Ctor.
     * @param bkt Bucket
     */
    S3Farm(final Bucket bkt) {
        this.bucket = bkt;
    }

    @Override
    public Iterable<Project> find(final String query) throws IOException {
        final Matcher matcher = S3Farm.QUERY.matcher(query);
        if (!matcher.matches()) {
            throw new IllegalArgumentException(
                String.format(
                    "Can't understand you: \"%s\"", query
                )
            );
        }
        final Collection<Project> list = new LinkedList<>();
        if ("id".equals(matcher.group(1))) {
            final String pid = matcher.group(2);
            list.addAll(this.findByXPath(String.format("id = '%s'", pid)));
            if (list.isEmpty()) {
                list.add(this.bootstrap(pid));
            }
        } else if ("link.github".equals(matcher.group(1))) {
            list.addAll(
                this.findByXPath(
                    String.format(
                        "link[@rel='github' and @href='%s']",
                        matcher.group(2)
                    )
                )
            );
        } else if (query.isEmpty()) {
            list.addAll(this.findByXPath(""));
        }
        return list;
    }

    @Override
    public void deploy(final Stakeholder stakeholder) throws IOException {
        stakeholder.work();
    }

    /**
     * Find a project by XPath query.
     * @param query XPath query
     * @return Projects found, if found
     * @throws IOException If fails
     */
    private Collection<Project> findByXPath(final String query)
        throws IOException {
        try (final Catalog catalog = this.catalog()) {
            return catalog
                .findByXPath(query)
                .stream()
                .map(
                    prefix -> new SyncProject(
                        new PoolProject(
                            new S3Project(this.bucket, prefix)
                        )
                    )
                )
                .collect(Collectors.toList());
        }
    }

    /**
     * Create a project with the given ID.
     * @param pid Project ID
     * @return Projects found, if found
     * @throws IOException If fails
     */
    private Project bootstrap(final String pid) throws IOException {
        try (final Catalog catalog = this.catalog()) {
            catalog.add(pid, S3Farm.prefix(pid));
        }
        return this.find(String.format("id=%s", pid)).iterator().next();
    }

    /**
     * Make a catalog.
     * @return Catalog
     * @throws IOException If fails
     */
    private Catalog catalog() throws IOException {
        final Catalog catalog = new Catalog(
            new S3Item(this.bucket.ocket("catalog.xml"))
        );
        catalog.bootstrap();
        return catalog;
    }

    /**
     * Create prefix from PID.
     * @param pid Project ID
     * @return Prefix to use
     */
    private static String prefix(final String pid) {
        return String.format(
            "%tY/%1$tm/%s/",
            new Date(),
            pid
        );
    }

}
