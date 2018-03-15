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
import com.jcabi.github.Issue;
import com.jcabi.github.Milestone;
import com.zerocracy.radars.github.Job;
import java.io.IOException;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * This rank give higher rank to jobs with an earlier due date
 * in milestone.
 * @author Kirill (g4s8.public@gmail.com)
 * @version $Id$
 * @since 0.22
 */
public final class RnkMilestone implements Comparator<String> {
    /**
     * Max date.
     */
    private static final Date MAX_DATE = new Date(Long.MAX_VALUE);
    /**
     * Github.
     */
    private final Github ghb;
    /**
     * Cache.
     */
    private final Map<String, Date> cache;
    /**
     * Ctor.
     * @param github Github
     */
    public RnkMilestone(final Github github) {
        this.ghb = github;
        this.cache = new HashMap<>(1);
    }

    @Override
    public int compare(final String left, final String right) {
        try {
            return Long.compare(
                this.dueDate(right).getTime(),
                this.dueDate(left).getTime()
            );
        } catch (final IOException err) {
            throw new IllegalStateException(err);
        }
    }

    /**
     * Read due date from milestone.
     * @param job Job id
     * @return Mileston due-date
     * @throws IOException If fails
     */
    private Date dueDate(final String job) throws IOException {
        final Date date;
        if (this.cache.containsKey(job)) {
            date = this.cache.get(job);
        } else if (job.startsWith("gh:")) {
            final Issue.Smart issue = new Issue.Smart(
                new Job.Issue(this.ghb, job)
            );
            if (issue.hasMilestone()) {
                date = new Milestone.Smart(issue.milestone()).dueOn();
            } else {
                date = RnkMilestone.MAX_DATE;
            }
            this.cache.put(job, date);
        } else {
            date = RnkMilestone.MAX_DATE;
        }
        return date;
    }
}
