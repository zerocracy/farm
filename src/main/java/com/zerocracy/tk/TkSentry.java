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
package com.zerocracy.tk;

import com.zerocracy.Farm;
import com.zerocracy.sentry.SafeSentry;
import org.takes.Take;
import org.takes.facets.fallback.TkFallback;
import org.takes.misc.Opt;
import org.takes.tk.TkWrap;

/**
 * Take to capture all errors in sentry.
 *
 * @since 1.0
 */
public final class TkSentry extends TkWrap {

    /**
     * Ctor.
     *
     * @param farm Farm
     * @param origin Origin take
     */
    public TkSentry(final Farm farm, final Take origin) {
        this(new SafeSentry(farm), origin);
    }

    /**
     * Ctor.
     *
     * @param sentry Sentry
     * @param origin Origin take
     */
    private TkSentry(final SafeSentry sentry, final Take origin) {
        super(
            new TkFallback(
                origin,
                err -> {
                    sentry.capture(err.throwable());
                    return new Opt.Empty<>();
                }
            )
        );
    }
}
