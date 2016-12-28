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

import com.jcabi.aspects.Tv;
import com.jcabi.s3.Bucket;
import com.jcabi.s3.Ocket;
import com.jcabi.xml.StrictXML;
import com.jcabi.xml.XMLDocument;
import com.jcabi.xml.XSD;
import com.jcabi.xml.XSDDocument;
import com.zerocracy.jstk.Farm;
import com.zerocracy.jstk.Project;
import com.zerocracy.jstk.Stakeholder;
import com.zerocracy.pm.Bootstrap;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.stream.Collectors;
import org.w3c.dom.Node;
import org.xembly.Directives;
import org.xembly.Xembler;

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
     * XSD for the list.
     */
    private static final XSD SCHEMA = XSDDocument.make(
        S3Farm.class.getResourceAsStream("list.xsd")
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
        final Collection<Project> list = new LinkedList<>();
        if (query.startsWith("id=")) {
            final String pid = query.substring(3);
            list.addAll(this.findByXPath(String.format("id=%s", pid)));
            if (list.isEmpty()) {
                list.add(this.bootstrap(pid));
            }
        } else if (query.startsWith("ref.github=")) {
            list.addAll(
                this.findByXPath(
                    String.format(
                        "ref[@rel='github']=%s",
                        query.substring(Tv.SEVEN)
                    )
                )
            );
        } else if (query.isEmpty()) {
            list.addAll(this.findByXPath(""));
        } else {
            throw new IllegalArgumentException(
                String.format(
                    "Can't understand you: \"%s\"", query
                )
            );
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
        return new XMLDocument(this.ocket().read())
            .xpath(String.format("//project[%s]/prefix/text()", query)).stream()
            .map(prefix -> new S3Project(this.bucket, prefix))
            .collect(Collectors.toList());
    }

    /**
     * Create a project with the given ID.
     * @param pid Project ID
     * @return Projects found, if found
     * @throws IOException If fails
     */
    private Project bootstrap(final String pid) throws IOException {
        final Ocket.Text ocket = this.ocket();
        final Node node = new StrictXML(
            new XMLDocument(ocket.read()),
            S3Farm.SCHEMA
        ).node();
        new Xembler(
            new Directives()
                .xpath("/projects").add("project")
                .add("id").set(pid).up()
                .add("created")
                .set(ZonedDateTime.now().format(DateTimeFormatter.ISO_INSTANT))
                .up()
                .add("prefix").set(S3Farm.prefix(pid))
        ).applyQuietly(node);
        ocket.write(
            new StrictXML(
                new XMLDocument(node),
                S3Farm.SCHEMA
            ).toString()
        );
        final Project project = this.find(pid).iterator().next();
        new Bootstrap(project).work();
        return project;
    }

    /**
     * Make an ocket.
     * @return Ocket
     */
    private Ocket.Text ocket() {
        return new Ocket.Text(this.bucket.ocket("list.xml"));
    }

    /**
     * Create prefix from PID.
     * @param pid Project ID
     * @return Prefix to use
     */
    private static String prefix(final String pid) {
        return String.format(
            "%tY/%1$tm/%s",
            new Date(),
            pid
        );
    }

}
