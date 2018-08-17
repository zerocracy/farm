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
package com.zerocracy.radars.slack;

import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import com.zerocracy.Farm;
import com.zerocracy.SoftException;
import com.zerocracy.farm.props.Props;
import com.zerocracy.sentry.SafeSentry;
import com.zerocracy.tools.TxtUnrecoverableError;
import java.io.IOException;
import org.cactoos.func.FuncOf;
import org.cactoos.func.FuncWithFallback;
import org.cactoos.func.IoCheckedFunc;

/**
 * Safe reaction.
 *
 * @since 1.0
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
    public boolean react(final Farm farm, final SlackMessagePosted event,
        final SkSession session) throws IOException {
        return new IoCheckedFunc<>(
            new FuncWithFallback<Boolean, Boolean>(
                smart -> {
                    boolean result = false;
                    try {
                        result = this.origin.react(farm, event, session);
                    } catch (final SoftException ex) {
                        session.send(
                            event.getChannel(), ex.getMessage()
                        );
                    }
                    return result;
                },
                new FuncOf<>(
                    throwable -> {
                        session.send(
                            event.getChannel(),
                            new TxtUnrecoverableError(
                                throwable, new Props(farm),
                                String.format(
                                    "Channel: %s, Sender: %s, EventType: %s",
                                    event.getChannel().getId(),
                                    event.getSender().getId(),
                                    event.getEventType().name()
                                )
                            ).asString()
                        );
                        new SafeSentry(farm).capture(throwable);
                        throw new IOException(throwable);
                    }
                )
            )
        ).apply(true);
    }

}
