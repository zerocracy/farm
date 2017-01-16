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
package com.zerocracy.tk;

import com.jcabi.log.VerboseProcess;
import com.zerocracy.jstk.Farm;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Queue;
import javax.json.JsonObject;
import org.takes.Take;
import org.takes.facets.auth.TkSecure;
import org.takes.facets.flash.TkFlash;
import org.takes.facets.fork.FkAuthenticated;
import org.takes.facets.fork.FkFixed;
import org.takes.facets.fork.FkHitRefresh;
import org.takes.facets.fork.FkRegex;
import org.takes.facets.fork.TkFork;
import org.takes.facets.forward.TkForward;
import org.takes.tk.TkClasspath;
import org.takes.tk.TkFiles;
import org.takes.tk.TkGzip;
import org.takes.tk.TkMeasured;
import org.takes.tk.TkVersioned;
import org.takes.tk.TkWithHeaders;
import org.takes.tk.TkWithType;
import org.takes.tk.TkWrap;

/**
 * Takes application.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.1
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class TkApp extends TkWrap {

    /**
     * Ctor, for tests mostly.
     * @param farm Farm
     * @throws IOException If fails
     */
    TkApp(final Farm farm) throws IOException {
        this(farm, new Properties(), new LinkedList<>());
    }

    /**
     * Ctor.
     * @param farm Farm
     * @param props Properties
     * @param events Events to dispatch
     * @throws IOException If fails
     */
    public TkApp(final Farm farm, final Properties props,
        final Queue<JsonObject> events) throws IOException {
        super(
            new TkWithHeaders(
                new TkVersioned(
                    new TkMeasured(
                        new TkGzip(
                            new TkFlash(
                                new TkAppAuth(
                                    new TkForward(
                                        TkApp.regex(farm, props, events)
                                    ),
                                    props
                                )
                            )
                        )
                    )
                ),
                String.format(
                    "X-Zerocracy-Revision: %s",
                    props.getProperty("build.revision")
                ),
                "Vary: Cookie"
            )
        );
    }

    /**
     * Make it.
     * @param farm Farm
     * @param props Properties
     * @param events Events to dispatch
     * @return Takes
     * @throws IOException If fails
     */
    private static Take regex(final Farm farm, final Properties props,
        final Queue<JsonObject> events) throws IOException {
        return new TkFork(
            new FkRegex("/", new TkIndex(props)),
            new FkRegex("/slack", new TkSlack(farm, props)),
            new FkAuthenticated(
                new TkSecure(
                    new TkFork(
                        new FkRegex("/alias", new TkAlias(farm))
                    )
                )
            ),
            new FkRegex("/ghook", new TkGhook(events)),
            new FkRegex("/robots.txt", ""),
            new FkRegex(
                "/xsl/[a-z\\-]+\\.xsl",
                new TkWithType(
                    TkApp.refresh("./src/main/xsl"),
                    "text/xsl"
                )
            ),
            new FkRegex(
                "/css/[a-z]+\\.css",
                new TkWithType(
                    TkApp.refresh("./src/main/scss"),
                    "text/css"
                )
            )
        );
    }

    /**
     * Hit refresh fork.
     * @param path Path of files
     * @return Fork
     * @throws IOException If fails
     */
    private static Take refresh(final String path) throws IOException {
        return new TkFork(
            new FkHitRefresh(
                new File(path),
                () -> new VerboseProcess(
                    new ProcessBuilder(
                        "mvn",
                        "generate-resources"
                    )
                ).stdout(),
                new TkFiles("./target/classes")
            ),
            new FkFixed(new TkClasspath())
        );
    }

}
