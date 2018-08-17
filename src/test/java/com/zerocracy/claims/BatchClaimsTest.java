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
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import org.junit.Test;
import org.mockito.Mockito;

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
        for (int idx = 0; idx < Tv.FIVE; ++idx) {
            claims.add(new ClaimOut().type("hello my future"));
        }
        final BatchClaims batch = Mockito.mock(BatchClaims.class);
        Mockito.doCallRealMethod().when(batch).close();
        Mockito.doCallRealMethod().when(batch).submit(Mockito.any());
        for (final ClaimOut claim : claims) {
            claim.postTo(batch);
        }
        Mockito.verify(batch, Mockito.times(Tv.FIVE)).close();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void sendClaimsInBatches()throws IOException {
        final List<ClaimOut> claims = new LinkedList<>();
        for (int idx = 0; idx < Tv.FIVE; ++idx) {
            claims.add(new ClaimOut());
        }
        final BatchClaims batch = Mockito.mock(BatchClaims.class);
        Mockito.doCallRealMethod().when(batch).close();
        Mockito.doCallRealMethod().when(batch).submit(Mockito.any());
        for (final ClaimOut claim : claims) {
            claim.postTo(batch);
        }
        Mockito.verify(batch, Mockito.times(Tv.THREE)).close();
    }
}
