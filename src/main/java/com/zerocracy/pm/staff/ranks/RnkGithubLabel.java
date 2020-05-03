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
import java.util.Set;
import org.cactoos.Func;
import org.cactoos.func.SolidFunc;
import org.cactoos.func.UncheckedFunc;

/**
 * Give higher rank for github tickets with 'bug' label.
 *
 * @since 1.0
 */
public final class RnkGithubLabel implements Comparator<String> {

    /**
     * Label name.
     */
    private final String label;

    /**
     * Issue labels by ticket.
     */
    private final UncheckedFunc<String, Set<String>> labels;

    /**
     * Ctor.
     * @param github Github
     * @param label Github label
     */
    public RnkGithubLabel(final Github github, final String label) {
        this(
            job -> new CachedIssues.Ext(github).value().labels(job),
            label
        );
    }

    /**
     * Ctor.
     * @param labels By github ticket
     * @param label Label to match
     */
    public RnkGithubLabel(final Func<String, Set<String>> labels,
        final String label) {
        this.labels = new UncheckedFunc<>(new SolidFunc<>(labels));
        this.label = label;
    }

    @Override
    public int compare(final String left, final String right) {
        return Boolean.compare(
            this.labels.apply(right).contains(this.label),
            this.labels.apply(left).contains(this.label)
        );
    }
}
