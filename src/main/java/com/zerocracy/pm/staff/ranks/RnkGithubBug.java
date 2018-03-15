/**
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

import com.jcabi.github.Github;
import com.jcabi.github.IssueLabels;
import com.zerocracy.radars.github.Job;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * Give higher rank for github tickets with 'bug' label.
 *
 * @author Kirill (g4s8.public@gmail.com)
 * @version $Id$
 * @since 0.21.1
 */
public final class RnkGithubBug implements Comparator<String> {

    /**
     * Function to check github bug label.
     */
    private final Github ghb;

    /**
     * Bug jobs cache.
     */
    private final Map<String, Boolean> cache;

    /**
     * Ctor.
     * @param github Github
     */
    public RnkGithubBug(final Github github) {
        this.ghb = github;
        this.cache = new HashMap<>(1);
    }

    @Override
    public int compare(final String left, final String right) {
        return Boolean.compare(this.isBug(right), this.isBug(left));
    }

    /**
     * Does this job have 'bug' label.
     * @param job Job id
     * @return True if has
     */
    private boolean isBug(final String job) {
        final boolean bug;
        if (this.cache.containsKey(job)) {
            bug = this.cache.get(job);
        } else {
            bug = job.startsWith("gh:") && new IssueLabels.Smart(
                new Job.Issue(this.ghb, job).labels()
            ).contains("bug");
            this.cache.put(job, bug);
        }
        return bug;
    }
}
