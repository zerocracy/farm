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
package com.zerocracy.farm;

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
import com.jcabi.xml.XML;
import com.zerocracy.jstk.Project;
import com.zerocracy.jstk.SoftException;
import com.zerocracy.jstk.Stakeholder;
import java.io.IOException;
import java.util.Properties;
import org.apache.commons.lang3.exception.ExceptionUtils;

/**
 * Stakeholder that mails failures.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.1
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class StkMailed implements Stakeholder {

    /**
     * Properties.
     */
    private final Properties props;

    /**
     * Original stakeholder.
     */
    private final Stakeholder origin;

    /**
     * Ctor.
     * @param pps Properties
     * @param stk Original stakeholder
     */
    public StkMailed(final Properties pps, final Stakeholder stk) {
        this.props = pps;
        this.origin = stk;
    }

    @Override
    @SuppressWarnings(
        {
            "PMD.AvoidCatchingThrowable",
            "PMD.AvoidRethrowingException"
        }
    )
    public void process(final Project project,
        final XML xml) throws IOException {
        try {
            this.origin.process(project, xml);
        } catch (final MismatchException | SoftException ex) {
            throw ex;
            // @checkstyle IllegalCatchCheck (1 line)
        } catch (final Throwable ex) {
            this.send(ex);
            throw ex;
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
    }

}
