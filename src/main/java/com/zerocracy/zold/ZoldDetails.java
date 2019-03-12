/*
 * Copyright (c) 2016-2019 Zerocracy
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
package com.zerocracy.zold;

import java.util.regex.Pattern;
import org.cactoos.Text;

/**
 * Zold details string.
 *
 * @since 1.0
 */
final class ZoldDetails implements Text {

    /**
     * Invalid zold details.
     */
    private static final Pattern PTN_INVALID =
        Pattern.compile("[^a-zA-Z0-9 @!?*_\\-.:,\'/]");

    /**
     * Zold details length.
     */
    private static final int MAX_LEN = 128;

    /**
     * Source.
     */
    private final String source;

    /**
     * Ctor.
     * @param source Source
     */
    ZoldDetails(final String source) {
        this.source = source;
    }

    @Override
    public String asString() {
        final String valid = ZoldDetails.PTN_INVALID.matcher(this.source)
            .replaceAll("-");
        final String truncated;
        if (valid.length() > ZoldDetails.MAX_LEN) {
            truncated = valid.substring(0, ZoldDetails.MAX_LEN);
        } else {
            truncated = valid;
        }
        return truncated;
    }
}
