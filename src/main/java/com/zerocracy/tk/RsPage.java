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
package com.zerocracy.tk;

import com.zerocracy.Farm;
import com.zerocracy.Xocument;
import com.zerocracy.farm.props.Props;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import org.cactoos.Scalar;
import org.cactoos.iterable.Mapped;
import org.cactoos.scalar.IoCheckedScalar;
import org.cactoos.scalar.Or;
import org.cactoos.scalar.Ternary;
import org.takes.Request;
import org.takes.Response;
import org.takes.facets.auth.Identity;
import org.takes.facets.auth.RqAuth;
import org.takes.facets.auth.XeIdentity;
import org.takes.facets.auth.XeLogoutLink;
import org.takes.facets.auth.social.XeGithubLink;
import org.takes.facets.flash.XeFlash;
import org.takes.facets.fork.FkTypes;
import org.takes.facets.fork.RsFork;
import org.takes.misc.Opt;
import org.takes.rs.RsPrettyXml;
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
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 * @checkstyle ClassFanOutComplexityCheck (500 lines)
 */
@SuppressWarnings("PMD.ExcessiveImports")
public final class RsPage extends RsWrap {

    /**
     * Ctor.
     * @param farm Farm
     * @param xsl XSL
     * @param req Request
     * @param src Source
     * @throws IOException If fails
     * @checkstyle ParameterNumberCheck (5 lines)
     */
    @SafeVarargs
    public RsPage(final Farm farm, final String xsl,
        final Request req, final Scalar<XeSource>... src) throws IOException {
        this(farm, xsl, req, Arrays.asList(src));
    }

    /**
     * Ctor.
     * @param farm Farm
     * @param xsl XSL
     * @param req Request
     * @param src Source
     * @throws IOException If fails
     * @checkstyle ParameterNumberCheck (5 lines)
     */
    public RsPage(final Farm farm, final String xsl, final Request req,
        final Iterable<Scalar<XeSource>> src) throws IOException {
        super(RsPage.make(farm, xsl, req, src));
    }

    /**
     * Make it.
     * @param farm Farm
     * @param xsl XSL
     * @param req Request
     * @param src Source
     * @return Response
     * @throws IOException If fails
     * @checkstyle ParameterNumberCheck (5 lines)
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private static Response make(final Farm farm, final String xsl,
        final Request req, final Iterable<Scalar<XeSource>> src)
        throws IOException {
        final Props props = new Props(farm);
        final Response raw = new RsXembly(
            new XeStylesheet(xsl),
            new XeAppend(
                "page",
                new XeMillis(false),
                new XeChain(
                    () -> {
                        final Collection<XeSource> sources = new LinkedList<>();
                        for (final Scalar<XeSource> item : src) {
                            sources.add(new IoCheckedScalar<>(item).value());
                        }
                        return sources;
                    }
                ),
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
                            props.get("//github/app.client_id", "")
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
                        props.get("//build/version", "1.0-SNAPSHOT")
                    ),
                    new XeAppend(
                        "revision",
                        props.get("//build/revision", "ffffff")
                    ),
                    new XeAppend(
                        "date",
                        props.get("//build/date", "2017-01-01")
                    )
                ),
                new XeAppend("datum", Xocument.VERSION),
                new XeMemory(),
                new XeMillis(true)
            )
        );
        final RsXslt html = new RsXslt(new RsWithType(raw, "text/html"));
        return new RsFork(
            req,
            request -> new IoCheckedScalar<>(
                new Ternary<Opt<Response>>(
                    () -> new Or(
                        new Mapped<>(
                            header -> new IoCheckedScalar<>(
                                () -> header.contains(
                                    "application/vnd.zerocracy+xml"
                                )
                            ),
                            request.head()
                        )
                    ).value(),
                    () -> new Opt.Empty<>(),
                    () -> new Opt.Single<>(html)
                )
            ).value(),
            new FkTypes(
                "*/*",
                new RsPrettyXml(new RsWithType(raw, "text/xml"))
            )
        );
    }

}
