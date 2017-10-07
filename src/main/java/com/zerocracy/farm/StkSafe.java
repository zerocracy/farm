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

import com.jcabi.xml.XML;
import com.zerocracy.farm.props.Props;
import com.zerocracy.jstk.Farm;
import com.zerocracy.jstk.Project;
import com.zerocracy.jstk.SoftException;
import com.zerocracy.jstk.Stakeholder;
import com.zerocracy.pm.ClaimIn;
import io.sentry.Sentry;
import java.io.IOException;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.cactoos.io.BytesOf;
import org.cactoos.text.FormattedText;
import org.cactoos.text.TextOf;

/**
 * Stakeholder that reports about failures and doesn't fail ever.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.1
 * @todo #272:30min Error handling in `StkSafe::process` method is similar to
 *  `ReSafe` in telegram, slack and github error handling. We need to refactor
 *  it. Let's use one class for it, now it's placed in `TxtUnrecoverableError`.
 */
@EqualsAndHashCode(of = "identifier")
public final class StkSafe implements Stakeholder {

    /**
     * Original stakeholder.
     */
    private final Stakeholder origin;

    /**
     * Farm.
     */
    private final Farm farm;

    /**
     * Stakeholder unique identifier.
     */
    private final String identifier;

    /**
     * Ctor.
     * @param sid Stakeholder identifier
     * @param frm Farm
     * @param stk Original stakeholder
     */
    public StkSafe(final String sid, final Farm frm, final Stakeholder stk) {
        this.identifier = sid;
        this.farm = frm;
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
                Sentry.capture(
                    new IllegalArgumentException(
                        String.format(
                            "Claim #%d \"%s\" has no token in %s",
                            claim.cid(), claim.type(), this.identifier
                        ),
                        ex
                    )
                );
            }
            // @checkstyle IllegalCatchCheck (1 line)
        } catch (final Throwable ex) {
            final Props props = new Props(this.farm);
            if (props.has("//testing")) {
                throw new IllegalStateException(ex);
            }
            if (claim.hasToken() && !"Notify".equals(claim.type())) {
                claim.reply(
                    String.join(
                        "",
                        "I can't do it for technical reasons, I'm very sorry.",
                        " If you don't know what to do,",
                        " submit this error as a ticket",
                        " [here](https://github.com/zerocracy/datum):\n\n```\n",
                        new FormattedText(
                            "%s %s %s\n%s\n%s",
                            props.get("//build/version"),
                            props.get("//build/revision"),
                            props.get("//build/date"),
                            ExceptionUtils.getMessage(ex),
                            StringUtils.abbreviate(
                                new TextOf(
                                    new BytesOf(
                                        ExceptionUtils.getRootCause(ex)
                                    )
                                ).asString(),
                                // @checkstyle MagicNumber (1 line)
                                1000
                            )
                        ).asString(),
                        "\n```"
                    )
                ).postTo(project);
            }
            Sentry.capture(
                new IllegalArgumentException(
                    String.format(
                        "Claim #%d in %s: type=\"%s\", id=\"%s\"",
                        claim.cid(), project, claim.type(),
                        this.identifier
                    ),
                    ex
                )
            );
        }
    }
}
