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
import com.zerocracy.Xocument;
import com.zerocracy.jstk.Farm;
import com.zerocracy.jstk.Item;
import com.zerocracy.jstk.Project;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.xembly.Directives;

/**
 * Farm in S3.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.1
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class S3Farm implements Farm {

    /**
     * PMO project name.
     */
    private static final String PMO = "PMO";

    /**
     * S3 bucket.
     */
    private final Bucket bucket;

    /**
     * Ctor.
     * @param bkt Bucket
     */
    public S3Farm(final Bucket bkt) {
        this.bucket = bkt;
    }

    @Override
    public Iterable<Project> find(final String xpath) throws IOException {
        try (final Item item = this.item()) {
            new Xocument(item.path()).bootstrap("pmo/catalog");
        }
        Iterable<Project> projects = this.findByXPath(xpath)
            .stream()
            .map(
                prefix -> new SyncProject(
                    new S3Project(this.bucket, prefix)
                )
            )
            .collect(Collectors.toList());
        if (!projects.iterator().hasNext()) {
            final Matcher matcher = Pattern.compile(
                "\\s*@id\\s*=\\s*'([^']+)'\\s*"
            ).matcher(xpath);
            if (matcher.matches()) {
                this.add(matcher.group(1));
                projects = this.find(xpath);
            }
        }
        return projects;
    }

    /**
     * Create a project with the given ID.
     * @param pid Project ID
     * @throws IOException If fails
     */
    private void add(final String pid) throws IOException {
        try (final Item item = this.item()) {
            new Xocument(item.path()).modify(
                new Directives()
                    .xpath("/catalog")
                    .add("project")
                    .attr("id", pid)
                    .add("created")
                    .set(
                        ZonedDateTime.now().format(
                            DateTimeFormatter.ISO_INSTANT
                        )
                    )
                    .up()
                    .add("prefix").set(S3Farm.prefix(pid))
            );
        }
    }

    /**
     * Find a project by XPath query.
     * @param xpath XPath query
     * @return Prefixes found, if found
     * @throws IOException If fails
     */
    private Collection<String> findByXPath(final String xpath)
        throws IOException {
        String term = xpath;
        if (!term.isEmpty()) {
            term = String.format("[%s]", term);
        }
        try (final Item item = this.item()) {
            return new Xocument(item).xpath(
                String.format("//project%s/prefix/text()", term)
            );
        }
    }

    /**
     * The item.
     * @return Item
     */
    private Item item() {
        return new S3Item(
            this.bucket.ocket(
                String.format("%scatalog.xml", S3Farm.prefix(S3Farm.PMO))
            )
        );
    }

    /**
     * Create prefix from PID.
     * @param pid Project ID
     * @return Prefix to use
     */
    private static String prefix(final String pid) {
        final String prefix;
        if (S3Farm.PMO.equals(pid)) {
            prefix = "PMO/";
        } else {
            prefix = String.format(
                "%tY/%1$tm/%s/",
                new Date(),
                pid
            );
        }
        return prefix;
    }

}
