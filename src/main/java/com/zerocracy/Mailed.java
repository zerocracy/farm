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
import com.zerocracy.farm.MismatchException;
import com.zerocracy.jstk.SoftException;
import java.io.IOException;
import java.util.Properties;
import org.apache.commons.lang3.exception.ExceptionUtils;

/**
 * Execution that mails a bug.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @param <T> Type of result
 * @since 0.11
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class Mailed<T> {

    /**
     * Properties.
     */
    private final Properties props;

    /**
     * The callable.
     */
    private final Mailed.Func<T> func;

    /**
     * Ctor.
     * @param pps Props
     * @param rnbl Runnable
     */
    public Mailed(final Properties pps, final Runnable rnbl) {
        this(
            pps,
            () -> {
                rnbl.run();
                return null;
            }
        );
    }

    /**
     * Ctor.
     * @param pps Props
     * @param proc Procedure
     */
    public Mailed(final Properties pps, final Mailed.Proc proc) {
        this(
            pps,
            () -> {
                proc.exec();
                return null;
            }
        );
    }

    /**
     * Ctor.
     * @param pps Props
     * @param fnc Func
     */
    public Mailed(final Properties pps, final Mailed.Func<T> fnc) {
        this.props = pps;
        this.func = fnc;
    }

    /**
     * Exec it.
     * @return The result
     * @throws IOException If fails
     */
    @SuppressWarnings(
        {
            "PMD.AvoidCatchingThrowable",
            "PMD.AvoidRethrowingException"
        }
    )
    public T exec() throws IOException {
        try {
            return this.func.exec();
        } catch (final MismatchException | SoftException ex) {
            throw ex;
            // @checkstyle IllegalCatchCheck (1 line)
        } catch (final Throwable ex) {
            this.mail(ex);
            throw ex;
        }
    }

    /**
     * Convert it to Runnable.
     * @return Runnable
     */
    public Runnable toRunnable() {
        return () -> {
            try {
                this.exec();
            } catch (final IOException ex) {
                throw new IllegalStateException(ex);
            }
        };
    }

    /**
     * Send this error by email.
     * @param error The error
     */
    @SuppressWarnings("PMD.AvoidDuplicateLiterals")
    private void mail(final Throwable error) {
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
        try {
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
        } catch (final IOException ioex) {
            throw new IllegalStateException(ioex);
        }
    }

    /**
     * Function to call.
     * @param <T> Type of result
     */
    public interface Func<T> {
        /**
         * Execute it.
         * @return The result
         * @throws IOException If fails
         */
        T exec() throws IOException;
    }

    /**
     * Procedure to exec.
     */
    public interface Proc {
        /**
         * Exec it.
         * @throws IOException If fails
         */
        void exec() throws IOException;
    }

}
