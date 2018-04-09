/**
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
package com.zerocracy.tools;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.cactoos.io.InputOf;
import org.cactoos.io.LengthOf;
import org.cactoos.io.OutputTo;
import org.cactoos.io.TeeInput;
import org.cactoos.text.TextOf;

/**
 * PGP signature for a piece of text.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.22
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class Signature {

    /**
     * The data to protect/sign.
     */
    private final String data;

    /**
     * Ctor.
     * @param dta The data to sign
     */
    public Signature(final String dta) {
        this.data = dta;
    }

    /**
     * PGP signature text.
     * @return PGP signature
     * @throws IOException If fails
     */
    public String asString() throws IOException {
        final String key = "0AAF4B5A";
        final Process receive = Signature.env(
            new ProcessBuilder(
                "gpg",
                "--no-options",
                "--keyserver",
                "hkp://ipv4.pool.sks-keyservers.net",
                "--verbose",
                "--recv-keys",
                key
            )
        ).start();
        try {
            receive.waitFor(1L, TimeUnit.MINUTES);
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(ex);
        }
        if (receive.exitValue() != 0) {
            throw new IllegalStateException(
                String.format(
                    "Failed to receive PGP key: %s",
                    new TextOf(receive.getErrorStream()).asString()
                )
            );
        }
        final Process sign = Signature.env(
            new ProcessBuilder(
                "gpg",
                "--batch",
                "--no-options",
                "--armor",
                "--local-user",
                key,
                "--recipient",
                key,
                "--detach-sig",
                "--sign"
            )
        ).start();
        new LengthOf(
            new TeeInput(
                new InputOf(this.data),
                new OutputTo(sign.getOutputStream())
            )
        ).intValue();
        try {
            receive.waitFor(1L, TimeUnit.MINUTES);
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(ex);
        }
        if (receive.exitValue() != 0) {
            throw new IllegalStateException(
                String.format(
                    "Failed to sign with PGP key: %s",
                    new TextOf(sign.getErrorStream()).asString()
                )
            );
        }
        return String.format(
            "\"%s\" encrypted by %s: %s",
            this.data,
            key,
            new TextOf(sign.getInputStream()).asString()
                .replace("-----BEGIN PGP SIGNATURE-----", "")
                .replace("-----END PGP SIGNATURE-----", "")
                .replaceAll("\n", "")
                .replaceAll("(?<=\\G.{8})", " ")
        );
    }

    /**
     * With environment.
     * @param builder Process builder
     * @return The same builder
     */
    private static ProcessBuilder env(final ProcessBuilder builder) {
        builder.environment().putIfAbsent(
            "GNUPGHOME", System.getProperty("java.io.tmpdir")
        );
        return builder.redirectInput(ProcessBuilder.Redirect.PIPE);
    }

}
