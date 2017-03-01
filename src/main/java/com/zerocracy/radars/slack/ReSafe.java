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

import com.jcabi.aspects.Tv;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import com.zerocracy.jstk.Farm;
import com.zerocracy.jstk.SoftException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.StringUtils;

/**
 * Safe reaction.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.1
 */
public final class ReSafe implements Reaction<SlackMessagePosted> {

    /**
     * Reaction.
     */
    private final Reaction<SlackMessagePosted> origin;

    /**
     * Ctor.
     * @param tgt Target
     */
    public ReSafe(final Reaction<SlackMessagePosted> tgt) {
        this.origin = tgt;
    }

    @Override
    @SuppressWarnings("PMD.AvoidCatchingThrowable")
    public boolean react(final Farm farm, final SlackMessagePosted event,
        final SlackSession session) throws IOException {
        boolean done = false;
        try {
            done = this.origin.react(farm, event, session);
        } catch (final SoftException ex) {
            session.sendMessage(event.getChannel(), ex.getMessage());
            // @checkstyle IllegalCatchCheck (1 line)
        } catch (final Throwable ex) {
            try (final ByteArrayOutputStream baos =
                new ByteArrayOutputStream()) {
                ex.printStackTrace(new PrintStream(baos));
                session.sendMessage(
                    event.getChannel(),
                    String.join(
                        "\n",
                        "There is an unrecoverable failure on my side.",
                        "Please, email this to bug@0crat.com:\n\n```",
                        StringUtils.abbreviate(
                            baos.toString(StandardCharsets.UTF_8),
                            Tv.THOUSAND
                        ),
                        "```"
                    )
                );
            }
            throw new IOException(ex);
        }
        return done;
    }

}
