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
package com.zerocracy.farm;

import com.jcabi.xml.XML;
import com.zerocracy.Farm;
import com.zerocracy.Par;
import com.zerocracy.Project;
import com.zerocracy.SoftException;
import com.zerocracy.Stakeholder;
import com.zerocracy.err.FbReaction;
import com.zerocracy.err.ReFallback;
import com.zerocracy.farm.props.Props;
import com.zerocracy.pm.ClaimIn;
import com.zerocracy.pm.ClaimOut;
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
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
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
     * Reaction with fallback.
     */
    private final FbReaction fbr;

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
        this.fbr = new FbReaction(false);
    }

    @Override
    @SuppressWarnings(
        {
            "PMD.AvoidCatchingThrowable",
            "PMD.AvoidRethrowingException",
            "PMD.CyclomaticComplexity"
        }
    )
    public void process(final Project project,
        final XML xml) throws IOException {
        final ClaimIn claim = new ClaimIn(xml);
        this.fbr.react(
            () -> {
                this.origin.process(project, xml);
                return true;
            },
            new StkFallback(claim, project)
        );
    }

    /**
     * Reaction fallback.
     */
    private class StkFallback implements ReFallback {
        /**
         * Claim.
         */
        private final ClaimIn clm;
        /**
         * Project.
         */
        private final Project proj;

        /**
         * Ctor.
         * @param claim A claim
         * @param project Project
         */
        StkFallback(
            final ClaimIn claim,
            final Project project
        ) {
            this.clm = claim;
            this.proj = project;
        }

        @Override
        public void process(final SoftException err)
            throws IOException {
            if (this.clm.hasToken()) {
                this.clm.reply(err.getMessage())
                    .postTo(this.proj);
            } else {
                Sentry.capture(
                    new IllegalArgumentException(
                        String.format(
                            "Claim #%d \"%s\" has no token in %s",
                            this.clm.cid(),
                            this.clm.type(),
                            StkSafe.this.identifier
                        ),
                        err
                    )
                );
            }
        }

        @Override
        public void process(final Exception err) throws IOException {
            if (MismatchException.class.equals(err.getClass())) {
                throw (IOException) err;
            }
            final StringBuilder msg = new StringBuilder(
                String.format(
                    // @checkstyle LineLength (1 line)
                    "Claim #%d in %s: type=\"%s\", stakeholder=\"%s\"",
                    this.clm.cid(), this.proj.pid(), this.clm.type(),
                    StkSafe.this.identifier
                )
            );
            if (this.clm.hasAuthor()) {
                msg.append(
                    String.format(", author=\"%s\"", this.clm.author())
                );
            }
            if (this.clm.hasToken()) {
                msg.append(
                    String.format(", token=\"%s\"", this.clm.token())
                );
            }
            new ClaimOut()
                .type("Error")
                .param("origin_id", this.clm.cid())
                .param("origin_type", this.clm.type())
                .param("message", msg.toString())
                .param("stacktrace", new TextOf(err).asString())
                .postTo(this.proj);
            final Props props = new Props(StkSafe.this.farm);
            if (props.has("//testing")) {
                throw new IllegalStateException(err);
            }
            Sentry.capture(err);
            if (
                this.clm.hasToken() && !this.clm.type().startsWith("Notify")
                ) {
                this.clm.reply(
                    new Par(
                        // @checkstyle LineLength (5 line)
                        "I can't do it for technical reasons, I'm very sorry.",
                        " If you don't know what to do,",
                        " submit this error as a ticket",
                        " [here](https://github.com/zerocracy/farm/issues):",
                        "\n\n```\n",
                        new FormattedText(
                            "%s %s %s\n%s\n%s",
                            props.get("//build/version", ""),
                            props.get("//build/revision", ""),
                            props.get("//build/date", ""),
                            ExceptionUtils.getMessage(err),
                            StringUtils.abbreviate(
                                new TextOf(new BytesOf(err)).asString(),
                                // @checkstyle MagicNumber (1 line)
                                1000
                            )
                        ).asString(),
                        "\n```\n\nCc @yegor256"
                    ).say()
                ).postTo(this.proj);
            }
        }
    }
}
