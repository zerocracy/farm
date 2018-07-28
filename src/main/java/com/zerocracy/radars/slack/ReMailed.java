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

import com.jcabi.log.Logger;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import com.zerocracy.Farm;
import org.cactoos.Func;
import org.cactoos.func.FuncOf;
import org.cactoos.func.FuncWithFallback;
import org.cactoos.func.UncheckedFunc;

/**
 * Mailed if exception.
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class ReMailed implements Reaction<SlackMessagePosted> {

    /**
     * Reaction.
     */
    private final Reaction<SlackMessagePosted> origin;

    /**
     * Ctor.
     * @param tgt Target
     */
    public ReMailed(final Reaction<SlackMessagePosted> tgt) {
        this.origin = tgt;
    }

    @Override
    public boolean react(final Farm farm, final SlackMessagePosted event,
        final SkSession session) {
        return new UncheckedFunc<>(
            new FuncWithFallback<>(
                (Func<Boolean, Boolean>) input -> this.origin.react(
                    farm, event, session
                ),
                new FuncOf<>(
                    throwable -> {
                        Logger.error(
                            this,
                            "%[exception]s",
                            throwable
                        );
                    }
                )
            )
        ).apply(true);
    }

}
