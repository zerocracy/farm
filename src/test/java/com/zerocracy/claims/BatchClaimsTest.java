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
package com.zerocracy.claims;

import com.jcabi.aspects.Tv;
import com.jcabi.xml.XML;
import com.jcabi.xml.XMLDocument;
import java.io.Closeable;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsEqual;
import org.junit.Test;
import org.xembly.Xembler;

/**
 * Tests for {@link BatchClaims}.
 *
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
public final class BatchClaimsTest {

    @Test(expected = UnsupportedOperationException.class)
    public void obeyClaimBatchMaxSize()throws IOException {
        final List<ClaimOut> claims = new LinkedList<>();
        final int count = Tv.FIVE;
        final ClaimOut claim = new ClaimOut();
        final int size = new XMLDocument(
            new Xembler(
                claim
            ).xmlQuietly()
        ).toString().length();
        for (int idx = 0; idx < count; ++idx) {
            claims.add(new ClaimOut());
        }
        final FkBatchClaims batch = new FkBatchClaims(new BatchClaims());
        final int perbatch = (int) Math.ceil((double) size / batch.maximum());
        for (final ClaimOut item : claims) {
            item.postTo(batch);
        }
        MatcherAssert.assertThat(
            "Called close() wrong number of times",
            batch.closes(),
            new IsEqual<>((int) Math.ceil((double) count / perbatch))
        );
    }

    /**
     * Decorator for {@link BatchClaims} testing.
     */
    private final class FkBatchClaims implements Claims, Closeable {

        /**
         * Origin.
         */
        private final BatchClaims origin;

        /**
         * Count of {@link BatchClaims#close()} invocations.
         */
        private int count;

        /**
         * Constructor.
         * @param origin The {@link BatchClaims} to be tested.
         */
        FkBatchClaims(final BatchClaims origin) {
            this.origin = origin;
        }

        @Override
        public void submit(final XML claim) throws IOException {
            this.origin.submit(claim);
        }

        @Override
        public void close() throws IOException {
            this.count = this.count + 1;
            this.origin.close();
        }

        /**
         * Return the maximum batch size (in KB) allowed.
         * @return Maximum batch size (in KB)
         */
        public int maximum() {
            return this.origin.maximum();
        }

        /**
         * Returns the times that the count method was invoked.
         * @return The times that the count method was invoked
         */
        public int closes() {
            return this.count;
        }
    }
}
