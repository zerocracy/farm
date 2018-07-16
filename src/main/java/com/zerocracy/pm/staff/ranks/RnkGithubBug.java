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

import com.jcabi.github.Github;
import com.jcabi.github.IssueLabels;
import com.zerocracy.radars.github.Job;
import com.zerocracy.radars.github.Quota;
import java.util.Comparator;
import org.cactoos.BiFunc;
import org.cactoos.func.StickyBiFunc;
import org.cactoos.func.SyncBiFunc;
import org.cactoos.func.UncheckedBiFunc;

/**
 * Give higher rank for github tickets with 'bug' label.
 *
 * @since 1.0
 */
public final class RnkGithubBug implements Comparator<String> {
    /**
     * Global bug jobs cache.
     */
    private static final BiFunc<Github, String, Boolean> CACHED =
        new UncheckedBiFunc<>(
            new SyncBiFunc<>(
                new StickyBiFunc<>(
                    (github, job) -> new Quota(github).quiet()
                        && job.startsWith("gh:") && new IssueLabels.Smart(
                            new Job.Issue(github, job).labels()
                        ).contains("bug")
                )
            )
        );

    /**
     * Function to check github bug label.
     */
    private final Github ghb;

    /**
     * Locally referenced cache.
     */
    private final UncheckedBiFunc<Github, String, Boolean> cache;

    /**
     * Ctor.
     * @param github Github
     */
    public RnkGithubBug(final Github github) {
        this(github, RnkGithubBug.CACHED);
    }

    /**
     * Ctor.
     * @param github Github
     * @param cache Cache function
     */
    RnkGithubBug(
        final Github github, final BiFunc<Github, String, Boolean> cache
    ) {
        this.ghb = github;
        this.cache = new UncheckedBiFunc<>(cache);
    }

    @Override
    public int compare(final String left, final String right) {
        return Boolean.compare(
            this.cache.apply(this.ghb, right), this.cache.apply(this.ghb, left)
        );
    }

}
