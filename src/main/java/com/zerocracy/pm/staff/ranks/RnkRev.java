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
package com.zerocracy.pm.staff.ranks;

import com.zerocracy.pm.scope.Wbs;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * Give higher rank for 'REV' jobs.
 *
 * @since 1.0
 */
public final class RnkRev implements Comparator<String> {
    /**
     * Role 'REV'.
     */
    private static final String REV = "REV";

    /**
     * WBS.
     */
    private final Wbs wbs;
    /**
     * REV jobs cache.
     */
    private final Map<String, Boolean> cache;

    /**
     * Ctor.
     * @param wbs WBS
     */
    public RnkRev(final Wbs wbs) {
        this.wbs = wbs;
        this.cache = new HashMap<>(1);
    }

    @SuppressWarnings("IfStatementWithTooManyBranches")
    @Override
    public int compare(final String left, final String right) {
        try {
            return Boolean.compare(this.isRev(right), this.isRev(left));
        } catch (final IOException err) {
            throw new IllegalStateException(err);
        }
    }

    /**
     * Does this job has REV role.
     * @param job A job to check
     * @return True if REV role
     * @throws IOException If fails
     */
    private boolean isRev(final String job) throws IOException {
        final boolean rev;
        if (this.cache.containsKey(job)) {
            rev = this.cache.get(job);
        } else {
            rev = RnkRev.REV.equals(this.wbs.role(job));
            this.cache.put(job, rev);
        }
        return rev;
    }
}
