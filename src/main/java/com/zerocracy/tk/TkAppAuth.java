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
import com.zerocracy.farm.props.Props;
import java.io.IOException;
import java.util.regex.Pattern;
import org.takes.Take;
import org.takes.facets.auth.PsByFlag;
import org.takes.facets.auth.PsChain;
import org.takes.facets.auth.PsCookie;
import org.takes.facets.auth.PsLogout;
import org.takes.facets.auth.TkAuth;
import org.takes.facets.auth.social.PsGithub;
import org.takes.facets.fork.FkFixed;
import org.takes.facets.fork.FkParams;
import org.takes.facets.fork.TkFork;
import org.takes.facets.previous.TkPrevious;
import org.takes.tk.TkRedirect;
import org.takes.tk.TkWrap;

/**
 * Authenticated application.
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
final class TkAppAuth extends TkWrap {

    /**
     * Ctor.
     * @param take Take
     * @param farm Farm
     * @throws IOException If fails
     */
    TkAppAuth(final Take take, final Farm farm) throws IOException {
        super(TkAppAuth.make(take, farm));
    }

    /**
     * Authenticated.
     * @param take Takes
     * @param farm Farm
     * @return Authenticated takes
     * @throws IOException If fails
     */
    @SuppressWarnings("PMD.AvoidDuplicateLiterals")
    private static Take make(final Take take, final Farm farm)
        throws IOException {
        final Props props = new Props(farm);
        return new TkAuth(
            new TkFork(
                new FkParams(
                    PsByFlag.class.getSimpleName(),
                    Pattern.compile(".+"),
                    new TkPrevious(new TkRedirect("/home"))
                ),
                new FkFixed(take)
            ),
            new PsChain(
                new PsByFlag(
                    new PsByFlag.Pair(
                        PsGithub.class.getSimpleName(),
                        new PsGithub(
                            props.get("//github/app.client_id", ""),
                            props.get("//github/app.client_secret", "")
                        )
                    ),
                    new PsByFlag.Pair(
                        PsLogout.class.getSimpleName(),
                        new PsLogout()
                    )
                ),
                new PsCookie(new CcSecure(farm))
            )
        );
    }

}
