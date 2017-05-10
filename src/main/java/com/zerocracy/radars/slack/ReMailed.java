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
package com.zerocracy.radars.slack;

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
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import com.zerocracy.jstk.Farm;
import java.io.IOException;
import java.util.Properties;
import org.apache.commons.lang3.exception.ExceptionUtils;

/**
 * Mailed if exception.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.11
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class ReMailed implements Reaction<SlackMessagePosted> {

    /**
     * Properties.
     */
    private final Properties props;

    /**
     * Reaction.
     */
    private final Reaction<SlackMessagePosted> origin;

    /**
     * Ctor.
     * @param pps Properties
     * @param tgt Target
     */
    public ReMailed(final Properties pps,
        final Reaction<SlackMessagePosted> tgt) {
        this.props = pps;
        this.origin = tgt;
    }

    @Override
    @SuppressWarnings("PMD.AvoidCatchingThrowable")
    public boolean react(final Farm farm, final SlackMessagePosted event,
        final SlackSession session) throws IOException {
        try {
            return this.origin.react(farm, event, session);
            // @checkstyle IllegalCatchCheck (1 line)
        } catch (final Throwable ex) {
            this.send(ex);
            throw new IOException(ex);
        }
    }

    /**
     * Mail it.
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
        postman.send(
            new Envelope.MIME()
                .with(new StSender("0crat <no-reply@0crat.com>"))
                .with(new StRecipient("0crat admin <bugs@0crat.com>"))
                .with(new StSubject(error.getLocalizedMessage()))
                .with(
                    new EnPlain(
                        String.format(
                            "Hi,\n\n%s\n\n--\n0crat\n%s %s %s",
                            ExceptionUtils.getStackTrace(error),
                            this.props.getProperty("build.version"),
                            this.props.getProperty("build.revision"),
                            this.props.getProperty("build.date")
                        )
                    )
                )
                .with(
                    new EnHTML(
                        String.format(
                            // @checkstyle LineLength (1 line)
                            "<html><body><p>Hi,</p><p>There was a problem:</p><pre>%s</pre><p>--<br/>0crat<br/>%s %s %s</p></body></html>",
                            ExceptionUtils.getStackTrace(error),
                            this.props.getProperty("build.version"),
                            this.props.getProperty("build.revision"),
                            this.props.getProperty("build.date")
                        )
                    )
                )
        );
        Logger.info(
            this, "%s emailed to admin: \"%s\"",
            error.getClass(), error.getLocalizedMessage()
        );
    }

}
