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

import com.jcabi.aspects.Tv;
import com.jcabi.log.Logger;
import com.jcabi.xml.XML;
import com.zerocracy.jstk.Project;
import com.zerocracy.jstk.SoftException;
import com.zerocracy.jstk.Stakeholder;
import com.zerocracy.pm.ClaimIn;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.StringUtils;

/**
 * Stakeholder that reports about failures.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.1
 */
public final class StkSafe implements Stakeholder {

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
    public StkSafe(final Properties pps, final Stakeholder stk) {
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
        final ClaimIn claim = new ClaimIn(xml);
        try {
            this.origin.process(project, xml);
        } catch (final MismatchException ex) {
            throw ex;
        } catch (final SoftException ex) {
            if (claim.hasToken()) {
                new ClaimIn(xml).reply(
                    String.format("Oops! %s", ex.getMessage())
                ).postTo(project);
            } else {
                Logger.error(
                    this, "%s soft failure at \"%s/%s\" in \"%s\": %s",
                    this.origin.getClass().getCanonicalName(),
                    new ClaimIn(xml).type(),
                    new ClaimIn(xml).number(),
                    project,
                    ex.getLocalizedMessage()
                );
            }
            // @checkstyle IllegalCatchCheck (1 line)
        } catch (final Throwable ex) {
            if (claim.hasToken()) {
                claim.reply(
                    String.join(
                        "",
                        "I can't do it for technical reasons, I'm very sorry.",
                        " If you don't know what to do,",
                        " submit this error as a ticket",
                        " [here](https://github.com/zerocracy/datum):\n\n```\n",
                        this.print(ex),
                        "\n```"
                    )
                ).postTo(project);
            }
            Logger.error(
                this, "%s failed at \"%s/%s\" in \"%s\": %[exception]s",
                this.origin.getClass().getCanonicalName(),
                new ClaimIn(xml).type(),
                new ClaimIn(xml).number(),
                project,
                ex
            );
        }
    }

    /**
     * Print exception to string.
     * @param error The error
     * @return Text
     * @throws IOException If fails
     */
    private String print(final Throwable error) throws IOException {
        try (final ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            error.printStackTrace(new PrintStream(baos));
            return String.format(
                "%s %s %s\n%s",
                this.props.getProperty("build.version"),
                this.props.getProperty("build.revision"),
                this.props.getProperty("build.date"),
                StringUtils.abbreviate(
                    baos.toString(StandardCharsets.UTF_8),
                    Tv.THOUSAND
                )
            );
        }
    }

}
