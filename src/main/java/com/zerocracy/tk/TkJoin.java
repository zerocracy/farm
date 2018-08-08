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
import java.io.IOException;
import org.takes.Response;
import org.takes.facets.fork.RqRegex;
import org.takes.facets.fork.TkRegex;

/**
 * Join Zerocracy form.
 *
 * @since 1.0
 *
 * @todo #1506:30min Each user should see the status of his/her resume at /join.
 *  If the resume is there, he/she should see the resume, not the form. For
 *  this, a resume page must be implemented in zerocracy-datum (pmo/resume
 *  .xsl), which will show the resume extracted from Resumes.This condition is
 *  already tested in TkJoinTest, in showResumeIfAlreadyApplied. When
 *  implemented, update the test and the ignore tag should be removed upon
 *  puzzle completion.
 * @checkstyle JavadocMethodCheck (500 lines)
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class TkJoin implements TkRegex {

    /**
     * Farm.
     */
    private final Farm farm;

    /**
     * Constructor.
     * @param frm Farm
     */
    public TkJoin(final Farm frm) {
        this.farm = frm;
    }

    @Override
    public Response act(final RqRegex req) throws IOException {
        return new RsPage(this.farm, "/xsl/join.xsl", req);
    }
}
