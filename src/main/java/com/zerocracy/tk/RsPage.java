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
package com.zerocracy.tk;

import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;
import org.takes.Request;
import org.takes.Response;
import org.takes.facets.auth.Identity;
import org.takes.facets.auth.RqAuth;
import org.takes.facets.auth.XeIdentity;
import org.takes.facets.auth.XeLogoutLink;
import org.takes.facets.auth.social.XeGithubLink;
import org.takes.facets.flash.XeFlash;
import org.takes.rs.RsWithType;
import org.takes.rs.RsWrap;
import org.takes.rs.RsXslt;
import org.takes.rs.xe.RsXembly;
import org.takes.rs.xe.XeAppend;
import org.takes.rs.xe.XeChain;
import org.takes.rs.xe.XeDate;
import org.takes.rs.xe.XeLinkHome;
import org.takes.rs.xe.XeLinkSelf;
import org.takes.rs.xe.XeLocalhost;
import org.takes.rs.xe.XeMemory;
import org.takes.rs.xe.XeMillis;
import org.takes.rs.xe.XeSla;
import org.takes.rs.xe.XeSource;
import org.takes.rs.xe.XeStylesheet;
import org.takes.rs.xe.XeWhen;

/**
 * Page.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.6
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@SuppressWarnings("PMD.ExcessiveImports")
final class RsPage extends RsWrap {

    /**
     * Ctor.
     * @param props Properties
     * @param xsl XSL
     * @param req Request
     * @param src Source
     * @throws IOException If fails
     * @checkstyle ParameterNumberCheck (5 lines)
     */
    RsPage(final Properties props, final String xsl,
        final Request req, final XeSource... src) throws IOException {
        this(props, xsl, req, Arrays.asList(src));
    }

    /**
     * Ctor.
     * @param props Properties
     * @param xsl XSL
     * @param req Request
     * @param src Source
     * @throws IOException If fails
     * @checkstyle ParameterNumberCheck (5 lines)
     */
    RsPage(final Properties props, final String xsl, final Request req,
        final Iterable<XeSource> src) throws IOException {
        super(RsPage.make(props, xsl, req, src));
    }

    /**
     * Make it.
     * @param props Properties
     * @param xsl XSL
     * @param req Request
     * @param src Source
     * @return Response
     * @throws IOException If fails
     * @checkstyle ParameterNumberCheck (5 lines)
     */
    private static Response make(final Properties props, final String xsl,
        final Request req, final Iterable<XeSource> src) throws IOException {
        final Response raw = new RsXembly(
            new XeStylesheet(xsl),
            new XeAppend(
                "page",
                new XeMillis(false),
                new XeChain(src),
                new XeLinkHome(req),
                new XeLinkSelf(req),
                new XeDate(),
                new XeSla(),
                new XeLocalhost(),
                new XeFlash(req),
                new XeWhen(
                    new RqAuth(req).identity().equals(Identity.ANONYMOUS),
                    new XeChain(
                        new XeGithubLink(
                            req,
                            props.getProperty("github.app.client_id", "")
                        )
                    )
                ),
                new XeWhen(
                    !new RqAuth(req).identity().equals(Identity.ANONYMOUS),
                    new XeChain(
                        new XeIdentity(req),
                        new XeLogoutLink(req)
                    )
                ),
                new XeAppend(
                    "version",
                    new XeAppend(
                        "name",
                        props.getProperty("build.version", "1.0-SNAPSHOT")
                    ),
                    new XeAppend(
                        "revision",
                        props.getProperty("build.revision", "ffffff")
                    ),
                    new XeAppend(
                        "date",
                        props.getProperty("build.date", "2017-01-01")
                    )
                ),
                new XeMemory(),
                new XeMillis(true)
            )
        );
        return new RsXslt(new RsWithType(raw, "text/html"));
    }

}
