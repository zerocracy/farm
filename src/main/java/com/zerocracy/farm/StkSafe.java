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
import com.zerocracy.Project;
import com.zerocracy.SoftException;
import com.zerocracy.Stakeholder;
import com.zerocracy.farm.props.Props;
import com.zerocracy.pm.ClaimIn;
import com.zerocracy.tools.TxtUnrecoverableError;
import io.sentry.Sentry;
import java.io.IOException;
import lombok.EqualsAndHashCode;
import org.cactoos.text.TextOf;

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
            "PMD.AvoidRethrowingException",
            "PMD.CyclomaticComplexity"
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
                new ClaimIn(xml).reply(ex.getMessage()).postTo(project);
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
            final StringBuilder msg = new StringBuilder(
                String.format(
                    "Claim #%d in %s: type=\"%s\", stakeholder=\"%s\"",
                    claim.cid(), project.pid(), claim.type(),
                    this.identifier
                )
            );
            if (claim.hasAuthor()) {
                msg.append(String.format(", author=\"%s\"", claim.author()));
            }
            if (claim.hasToken()) {
                msg.append(String.format(", token=\"%s\"", claim.token()));
            }
            final Props props = new Props(this.farm);
            if (props.has("//testing")) {
                throw new IllegalStateException(ex);
            }
            claim.copy()
                .type("Error")
                .param("origin_id", claim.cid())
                .param("origin_type", claim.type())
                .param("message", msg.toString())
                .param("stacktrace", new TextOf(ex).asString())
                .postTo(project);
            Sentry.capture(ex);
            if (claim.hasToken() && !claim.type().startsWith("Notify")) {
                claim.reply(
                    new TxtUnrecoverableError(ex, props).asString()
                ).postTo(project);
            }
        }
    }
}
