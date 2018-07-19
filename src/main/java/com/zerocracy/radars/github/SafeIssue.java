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
package com.zerocracy.radars.github;

import com.jcabi.github.Comments;
import com.jcabi.github.Event;
import com.jcabi.github.Issue;
import com.jcabi.github.IssueLabels;
import com.jcabi.github.Repo;
import java.io.IOException;
import javax.json.JsonObject;

/**
 * Safe GitHub issue.
 *
 * @since 1.0
 */
@SuppressWarnings("PMD.TooManyMethods")
final class SafeIssue implements Issue {

    /**
     * Original issue.
     */
    private final transient Issue origin;

    /**
     * Ctor.
     * @param issue Original issue
     */
    SafeIssue(final Issue issue) {
        this.origin = issue;
    }

    @Override
    public boolean equals(final Object obj) {
        return this.origin.equals(obj);
    }

    @Override
    public int hashCode() {
        return this.origin.hashCode();
    }

    @Override
    public JsonObject json() throws IOException {
        return this.origin.json();
    }

    @Override
    public Repo repo() {
        return this.origin.repo();
    }

    @Override
    public int number() {
        return this.origin.number();
    }

    @Override
    public Comments comments() {
        return this.origin.comments();
    }

    @Override
    public IssueLabels labels() {
        return this.origin.labels();
    }

    @Override
    public Iterable<Event> events() throws IOException {
        return this.origin.events();
    }

    @Override
    public boolean exists() throws IOException {
        return this.origin.exists();
    }

    @Override
    public int compareTo(final Issue issue) {
        return this.origin.compareTo(issue);
    }

    @Override
    public void patch(final JsonObject json) throws IOException {
        this.origin.patch(json);
    }

}
