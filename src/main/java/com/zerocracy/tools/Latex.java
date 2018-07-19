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
package com.zerocracy.tools;

import com.jcabi.http.request.JdkRequest;
import com.jcabi.http.response.RestResponse;
import com.zerocracy.farm.props.Props;
import java.io.IOException;
import java.net.HttpURLConnection;
import javax.json.Json;
import javax.json.JsonObject;
import org.cactoos.Input;
import org.cactoos.io.InputOf;
import org.cactoos.text.TextOf;

/**
 * LaTeX document.
 *
 * @since 1.0
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
                "\n\\def\\pgp{%s}\n%s\n",
                new Signature(this.data).asString(),
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
            .add("email", "latex@zerocracy.com")
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

}
