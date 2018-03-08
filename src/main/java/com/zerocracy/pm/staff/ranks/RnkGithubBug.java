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
import java.io.IOException;
import java.util.Comparator;
import org.cactoos.Func;
import org.cactoos.func.IoCheckedFunc;
import org.cactoos.func.StickyFunc;

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
    private final Func<String, Boolean> bug;
    /**
     * Ctor.
     * @param github Github
     */
    public RnkGithubBug(final Github github) {
        this.bug = new StickyFunc<>(new RnkGithubBug.IsJob(github));
    }

    @Override
    public int compare(final String left, final String right) {
        try {
            return Boolean.compare(
                new IoCheckedFunc<>(this.bug).apply(right),
                new IoCheckedFunc<>(this.bug).apply(left)
            );
        } catch (final IOException err) {
            throw new IllegalArgumentException(err);
        }
    }

    /**
     * Is a job function.
     */
    private static final class IsJob implements Func<String, Boolean> {
        /**
         * Github.
         */
        private final Github ghb;
        /**
         * Ctor.
         * @param github Github
         */
        private IsJob(final Github github) {
            this.ghb = github;
        }
        @Override
        public Boolean apply(final String input) throws Exception {
            return input.startsWith("gh:") && new IssueLabels.Smart(
                new Job.Issue(this.ghb, input).labels()
            ).contains("bug");
        }
    }
}
