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
package com.zerocracy.entry;

import com.jcabi.xml.XML;
import com.zerocracy.claims.Claims;
import java.io.IOException;

/**
 * Validation decorator for claims.
 * <p>
 * It will not accept too big claims and prevent from
 * take too many claims at once, see
 * <a href="https://docs.aws.amazon.com/AWSSimpleQueueService/latest/SQSDeveloperGuide/sqs-limits.html">
 *     AWS SQS restrictions
 * </a>
 *
 * @since 1.0
 * @checkstyle MagicNumberCheck (500 lines)
 */
public final class ValidClaims implements Claims {

    /**
     * Max claim size is 256 KB.
     */
    private static final int MAX_CLAIM_LENGTH = 256 << 10;

    /**
     * Origin.
     */
    private final Claims claims;

    /**
     * Ctor.
     *
     * @param claims Origin claims
     */
    public ValidClaims(final Claims claims) {
        this.claims = claims;
    }

    @Override
    public void submit(final XML claim) throws IOException {
        final int size = claim.toString().length();
        if (size > ValidClaims.MAX_CLAIM_LENGTH) {
            throw new IllegalArgumentException(
                String.format(
                    "Claim is too big: %d bytes, max claim size is %d bytes",
                    size,
                    ValidClaims.MAX_CLAIM_LENGTH
                )
            );
        }
        this.claims.submit(claim);
    }
}
