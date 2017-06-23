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
package com.zerocracy;

import com.jcabi.email.Envelope;
import com.jcabi.email.Postman;
import com.jcabi.email.Protocol;
import com.jcabi.email.Token;
import com.jcabi.email.enclosure.EnHTML;
import com.jcabi.email.enclosure.EnPlain;
import com.jcabi.email.stamp.StRecipient;
import com.jcabi.email.stamp.StSender;
import com.jcabi.email.stamp.StSubject;
import com.jcabi.email.wire.SMTP;
import com.jcabi.log.Logger;
import java.io.IOException;
import java.util.Properties;
import org.cactoos.Func;
import org.cactoos.text.BytesAsText;
import org.cactoos.text.ThrowableAsBytes;
import org.cactoos.text.UncheckedText;

/**
 * Func that mails the exception.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.11
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class ThrowableToEmail implements Func<Throwable, Boolean> {

    /**
     * Props.
     */
    private final Properties props;

    /**
     * Ctor.
     * @param pps Props
     */
    public ThrowableToEmail(final Properties pps) {
        this.props = pps;
    }

    @Override
    public Boolean apply(final Throwable error) throws IOException {
        if (this.props.containsKey("testing")) {
            Logger.warn(this, "%s", error.getLocalizedMessage());
        } else {
            this.send(error);
        }
        throw new IllegalStateException(error);
    }

    /**
     * Send email.
     * @param error The error
     * @throws IOException If fails
     */
    private void send(final Throwable error) throws IOException {
        final Postman postman = new Postman.Default(
            new SMTP(
                new Token(
                    this.props.getProperty("smtp.username"),
                    this.props.getProperty("smtp.password")
                ).access(
                    new Protocol.SMTP(
                        this.props.getProperty("smtp.host"),
                        Integer.parseInt(this.props.getProperty("smtp.port"))
                    )
                )
            )
        );
        final String version = String.format(
            "%s %s %s",
            this.props.getProperty("build.version"),
            this.props.getProperty("build.revision"),
            this.props.getProperty("build.date")
        );
        postman.send(
            new Envelope.MIME()
                .with(new StSender("0crat <no-reply@0crat.com>"))
                .with(new StRecipient("0crat admin <bugs@0crat.com>"))
                .with(new StSubject(error.getLocalizedMessage()))
                .with(
                    new EnPlain(
                        String.format(
                            "Hi,\n\n%s\n\n--\n0crat\n%s",
                            new UncheckedText(
                                new BytesAsText(
                                    new ThrowableAsBytes(error)
                                )
                            ).asString(),
                            version
                        )
                    )
                )
                .with(
                    new EnHTML(
                        String.format(
                            // @checkstyle LineLength (1 line)
                            "<html><body><p>Hi,</p><p>There was a problem:</p><pre>%s</pre><p>--<br/>0crat<br/>%s</p></body></html>",
                            new UncheckedText(
                                new BytesAsText(
                                    new ThrowableAsBytes(error)
                                )
                            ).asString(),
                            version
                        )
                    )
                )
        );
    }

}
