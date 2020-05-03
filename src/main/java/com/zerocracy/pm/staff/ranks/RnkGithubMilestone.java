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
package com.zerocracy.pm.staff.ranks;

import com.jcabi.github.Github;
import com.zerocracy.gh.CachedIssues;
import java.util.Comparator;
import org.cactoos.func.SolidFunc;
import org.cactoos.func.UncheckedFunc;

/**
 * Give higher rank to Github issues with milestone with
 * earlier {@code due_date} field.
 *
 * @since 1.0
 * @todo #575:30min This rank just check that issue is milestoned,
 *  but it should check it's due-date param, it can be obtained from
 *  milestones.xml by repo coordinates and milestone title.
 */
public final class RnkGithubMilestone implements Comparator<String> {

    /**
     * Milestone cache.
     */
    private final UncheckedFunc<String, Boolean> cache;

    /**
     * Ctor.
     *
     * @param github Github
     */
    public RnkGithubMilestone(final Github github) {
        this.cache = new UncheckedFunc<>(
            new SolidFunc<>(
                job -> new CachedIssues.Ext(github).value().hasMilestone(job)
            )
        );
    }

    @Override
    public int compare(final String left, final String right) {
        return Boolean.compare(
            this.cache.apply(right),
            this.cache.apply(left)
        );
    }
}
