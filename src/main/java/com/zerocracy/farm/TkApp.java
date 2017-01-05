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

import com.github.rjeschke.txtmark.Configuration;
import com.github.rjeschke.txtmark.Processor;
import com.jcabi.log.Logger;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.takes.Take;
import org.takes.facets.fork.FkRegex;
import org.takes.facets.fork.TkFork;
import org.takes.facets.fork.TkRegex;
import org.takes.rs.RsVelocity;
import org.takes.tk.TkWrap;

/**
 * Takes application.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.1
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
final class TkApp extends TkWrap {

    /**
     * When we started.
     */
    private static final long STARTED = System.currentTimeMillis();

    /**
     * Ctor.
     * @param version App version
     * @throws IOException If fails
     */
    TkApp(final String version) throws IOException {
        super(TkApp.make(version));
    }

    /**
     * Ctor.
     * @param version App version
     * @return Takes
     */
    private static Take make(final String version) {
        return new TkFork(
            new FkRegex("/robots.txt", ""),
            new FkRegex(
                "/",
                (Take) req -> new RsVelocity(
                    TkApp.class.getResource("/html/index.html"),
                    new RsVelocity.Pair("version", version),
                    new RsVelocity.Pair(
                        "alive",
                        Logger.format(
                            "%[ms]s",
                            System.currentTimeMillis() - TkApp.STARTED
                        )
                    )
                )
            ),
            new FkRegex(
                "/([a-z\\-]+)\\.html",
                (TkRegex) req -> new RsVelocity(
                    TkApp.class.getResource("/layout/page.html"),
                    new RsVelocity.Pair("title", req.matcher().group(1)),
                    new RsVelocity.Pair(
                        "html",
                        Processor.process(
                            IOUtils.toString(
                                TkApp.class.getResource(
                                    String.format(
                                        "/pages/%s.md",
                                        req.matcher().group(1)
                                    )
                                ),
                                StandardCharsets.UTF_8
                            ),
                            Configuration.DEFAULT_SAFE
                        )
                    )
                )
            )
        );
    }

}
