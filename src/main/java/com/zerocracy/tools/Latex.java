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
import java.io.IOException;
import org.cactoos.Input;
import org.cactoos.io.InputOf;
import org.cactoos.text.TextOf;

/**
 * LaTeX document.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.20
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class Latex {

    /**
     * The source.
     */
    private final Input source;

    /**
     * Ctor.
     * @param src LaTeX source
     */
    public Latex(final String src) {
        this(new InputOf(src));
    }

    /**
     * Ctor.
     * @param src LaTeX source
     */
    public Latex(final Input src) {
        this.source = src;
    }

    /**
     * Create PDF file.
     * @return PDF file location
     * @throws IOException If fails
     */
    public Input pdf() throws IOException {
        final String boundary = "-------------------------748329778239743829";
        final String body = String.join(
            "",
            boundary, "\n",
            "Content-Disposition: form-data; name=\"pole\"",
            new TextOf(this.source).asString()
        );
        final byte[] bytes = new JdkRequest("https://tex.mendelu.cz/en/")
            .method("POST")
            .header(
                "Content-type",
                String.format(
                    "multipart/form-data; boundary=%s", boundary
                )
            )
            .header("Content-Length", body.getBytes().length)
            .header("Content-Encoding", "utf-8")
            .body().set(body).back()
            .fetch()
            .binary();
        return new InputOf(bytes);
    }

}
