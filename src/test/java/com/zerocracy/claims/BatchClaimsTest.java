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
import com.zerocracy.Farm;
import com.zerocracy.Item;
import com.zerocracy.Project;
import com.zerocracy.Xocument;
import com.zerocracy.entry.ClaimsOf;
import com.zerocracy.farm.fake.FkFarm;
import com.zerocracy.farm.fake.FkProject;
import org.junit.Test;
import org.xembly.Directives;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Tests for {@link BatchClaims}.
 *
 * @since 1.0
 */
public class BatchClaimsTest {

    @Test(expected = UnsupportedOperationException.class)
    public void obeyClaimBatchMaxSize()throws IOException {
        final Project project = new FkProject();
        final int total = Tv.HUNDRED;
        final List<ClaimOut> claims = new LinkedList<>();
        for (int idx = 0; idx < total; ++idx) {
            claims.add(new ClaimOut().type("hello my future"));
        }
        Claims claims = new BatchClaims();
        for (XML claim : new ClaimsItem(project).iterate()){
            claims.submit(claim);
        }
    }
}
