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

import com.zerocracy.Par;
import com.zerocracy.SoftException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import org.takes.Response;
import org.takes.Take;
import org.takes.facets.flash.RsFlash;
import org.takes.facets.forward.RsForward;
import org.takes.rs.RsWrap;
import org.takes.tk.TkWrap;

/**
 * Forward to home on soft exception.
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
final class TkSoftForward extends TkWrap {

    /**
     * Ctor.
     * @param take Take
     */
    @SuppressWarnings("PMD.PreserveStackTrace")
    TkSoftForward(final Take take) {
        super(request -> {
            try {
                return new TkSoftForward.RsSoft(take.act(request));
            } catch (final SoftException ex) {
                throw TkSoftForward.error(ex);
            }
        });
    }

    /**
     * The exception to throw.
     * @param error Original error
     * @return New exception
     * @throws UnsupportedEncodingException If fails
     */
    private static IOException error(final SoftException error)
        throws UnsupportedEncodingException {
        return new RsForward(
            new RsFlash(
                new Par.ToText(error.getLocalizedMessage()).toString(),
                Level.SEVERE
            )
        );
    }

    /**
     * Safe response.
     */
    private static final class RsSoft extends RsWrap {
        /**
         * Ctor.
         * @param res Response original
         */
        RsSoft(final Response res) {
            super(
                new Response() {
                    @Override
                    public Iterable<String> head() throws IOException {
                        try {
                            return res.head();
                        } catch (final SoftException ex) {
                            throw TkSoftForward.error(ex);
                        }
                    }
                    @Override
                    public InputStream body() throws IOException {
                        try {
                            return res.body();
                        } catch (final SoftException ex) {
                            throw TkSoftForward.error(ex);
                        }
                    }
                }
            );
        }
    }

}
