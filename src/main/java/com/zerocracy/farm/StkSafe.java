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
import io.sentry.Sentry;
import java.io.IOException;
import java.util.Properties;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;
import org.cactoos.text.BytesAsText;
import org.cactoos.text.FormattedText;
import org.cactoos.text.ThrowableAsBytes;

/**
 * Stakeholder that reports about failures and doesn't fail ever.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.1
 */
@EqualsAndHashCode(of = "identifier")
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
     * Stakeholder unique identifier.
     */
    @SuppressWarnings(
        {
            "PMD.SingularField",
            "PMD.UnusedPrivateField"
        }
    )
    private final String identifier;

    /**
     * Ctor.
     * @param id Identifier
     * @param pps Properties
     * @param stk Original stakeholder
     */
    public StkSafe(final String id, final Properties pps,
        final Stakeholder stk) {
        this.identifier = id;
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
                        new FormattedText(
                            "%s %s %s\n%s",
                            this.props.getProperty("build.version"),
                            this.props.getProperty("build.revision"),
                            this.props.getProperty("build.date"),
                            StringUtils.abbreviate(
                                new BytesAsText(
                                    new ThrowableAsBytes(ex)
                                ).asString(),
                                Tv.THOUSAND
                            )
                        ).asString(),
                        "\n```"
                    )
                ).postTo(project);
            }
            Sentry.capture(ex);
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
}
