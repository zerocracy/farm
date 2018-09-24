/*
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
import com.zerocracy.claims.ClaimIn;
import com.zerocracy.entry.ClaimsOf;
import com.zerocracy.farm.props.Props;
import com.zerocracy.sentry.SafeSentry;
import com.zerocracy.tools.TxtUnrecoverableError;
import java.io.IOException;
import lombok.EqualsAndHashCode;
import org.cactoos.text.TextOf;

/**
 * Stakeholder that reports about failures and doesn't fail ever.
 *
 * @since 1.0
 * @checkstyle CyclomaticComplexityCheck (500 lines)
 */
@EqualsAndHashCode(of = "identifier")
@SuppressWarnings(
    {"PMD.ModifiedCyclomaticComplexity", "PMD.StdCyclomaticComplexity"}
)
public final class StkSafe implements Stakeholder {

    /**
     * Max stacktrace length.
     */
    private static final int STACKTRACE_MAX = 8192;

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

    // @todo #733:30min Prevent StkSafe from swallowing exceptions generated
    //  in tests. It was discovered in #733 that StkSafe is treating all
    //  exceptions requests in the same way: thay're wrapped in an notify
    //  claim and processed. This causes tests to never fail when throwing
    //  exceptions: the exception which should break the test is treated like
    //  a notification and does not breaks it. We should create a way to
    //  avoid this exception swallowing.
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
                new ClaimIn(xml).reply(ex.getMessage()).postTo(
                    new ClaimsOf(this.farm, project)
                );
            } else {
                new SafeSentry(this.farm).capture(
                    new IllegalArgumentException(
                        String.format(
                            "Claim #%s \"%s\" has no token in %s",
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
                    "Claim #%s in %s: type=\"%s\", stakeholder=\"%s\"",
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
            if (!claim.isError()) {
                claim.copy()
                    .type("Error")
                    .param("origin_id", claim.cid())
                    .param("origin_type", claim.type())
                    .param("message", msg.toString())
                    .param("stacktrace", StkSafe.stacktrace(ex))
                    .postTo(new ClaimsOf(this.farm, project));
            }
            new SafeSentry(this.farm).capture(ex);
            if (claim.hasToken() && !claim.type().startsWith("Notify")) {
                claim.reply(
                    new TxtUnrecoverableError(
                        ex, props,
                        String.format(
                            // @checkstyle LineLength (1 line)
                            "CID: [%s](https://www.0crat.com/%s/%1$d), Type: \"%s\", Author: \"%s\"",
                            claim.cid(), project.pid(), claim.type(),
                            claim.author()
                        )
                    ).asString()
                ).postTo(new ClaimsOf(this.farm, project));
            }
        }
    }

    /**
     * Stacktrace for error.
     * @param exception Error
     * @return Stacktrace
     * @throws IOException If fails
     */
    private static String stacktrace(final Throwable exception)
        throws IOException {
        final String error = new TextOf(exception).asString();
        final String stacktrace;
        if (error.length() > StkSafe.STACKTRACE_MAX) {
            stacktrace = error.substring(0, StkSafe.STACKTRACE_MAX);
        } else {
            stacktrace = error;
        }
        return stacktrace;
    }
}
