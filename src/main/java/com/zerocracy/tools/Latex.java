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

import com.jcabi.http.request.JdkRequest;
import com.jcabi.http.response.RestResponse;
import com.zerocracy.farm.props.Props;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.concurrent.TimeUnit;
import javax.json.Json;
import javax.json.JsonObject;
import org.cactoos.Input;
import org.cactoos.io.InputOf;
import org.cactoos.io.LengthOf;
import org.cactoos.io.OutputTo;
import org.cactoos.io.TeeInput;
import org.cactoos.text.TextOf;

/**
 * LaTeX document.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.20
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class Latex {

    /**
     * The source.
     */
    private final String source;

    /**
     * The data to protect/sign.
     */
    private final String data;

    /**
     * Ctor.
     * @param src LaTeX source
     * @param dta The data to sign
     */
    public Latex(final String src, final String dta) {
        this.source = src;
        this.data = dta;
    }

    /**
     * Create PDF file.
     * @return PDF file location
     * @throws IOException If fails
     */
    public Input pdf() throws IOException {
        final String latex = this.source.replace(
            "\\begin{document}",
            String.format(
                "\\def\\pgp{%s: %s}\n%s",
                this.data,
                this.pgp(),
                new TextOf(
                    Latex.class.getResource("layout.tex")
                ).asString()
            )
        );
        final JsonObject req = Json.createObjectBuilder()
            .add("apikey", new Props().get("//cloudconvert/key", ""))
            .add("inputformat", "tex")
            .add("outputformat", "pdf")
            .add("wait", true)
            .add("input", "raw")
            .add("download", "inline")
            .add("filename", "article.tex")
            .add("file", latex)
            .build();
        final String body = req.toString();
        final String uri = "https://api.cloudconvert.com/convert";
        final String loc = new JdkRequest(uri)
            .method("POST")
            .header("Content-type", "application/json")
            .header("Content-Length", body.getBytes("UTF-8").length)
            .body().set(body).back()
            .fetch()
            .as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_SEE_OTHER)
            .headers()
            .get("Location")
            .get(0);
        return new InputOf(
            new JdkRequest(loc)
                .fetch()
                .as(RestResponse.class)
                .assertStatus(HttpURLConnection.HTTP_OK)
                .binary()
        );
    }

    /**
     * PGP signature text.
     * @return PGP signature
     * @throws IOException If fails
     */
    private String pgp() throws IOException {
        final Process receive = new ProcessBuilder(
            "gpg",
            "--keyserver",
            "hkp://ipv4.pool.sks-keyservers.net",
            "--verbose",
            "--recv-keys",
            "0AAF4B5A"
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
        final Process sign = new ProcessBuilder(
            "gpg",
            "--batch",
            "--armor",
            "--local-user",
            "0AAF4B5A",
            "--recipient",
            "0AAF4B5A",
            "--detach-sig",
            "--sign"
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
        return new TextOf(sign.getInputStream()).asString()
            .replace("-----BEGIN PGP SIGNATURE-----", "")
            .replace("-----END PGP SIGNATURE-----", "")
            .replaceAll("\n", "");
    }

}
