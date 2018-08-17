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
package com.zerocracy.sentry;

import com.zerocracy.Farm;
import com.zerocracy.farm.props.Props;
import io.sentry.SentryClientFactory;
import org.cactoos.func.SolidFunc;
import org.cactoos.func.UncheckedFunc;

/**
 * Sentry for farm.
 *
 * @since 1.0
 */
public final class SentryOf implements Sentry {

    /**
     * Sentry for farm.
     */
    private static final UncheckedFunc<Farm, Sentry> SENTRY =
        new UncheckedFunc<>(
            new SolidFunc<>(
                farm -> {
                    final Sentry sentry;
                    if (new Props(farm).has("//testing")) {
                        sentry = Sentry.FAKE;
                    } else {
                        sentry = new SentryWithClient(
                            SentryClientFactory.sentryClient()
                        );
                    }
                    return sentry;
                }
            )
        );

    /**
     * Farm.
     */
    private final Farm frm;

    /**
     * Ctor.
     *
     * @param farm Farm
     */
    public SentryOf(final Farm farm) {
        this.frm = farm;
    }

    @Override
    public void capture(final Throwable error) {
        SentryOf.SENTRY.apply(this.frm).capture(error);
    }
}
