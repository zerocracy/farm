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
import com.jcabi.github.Coordinates;
import com.jcabi.github.Event;
import com.jcabi.github.Github;
import com.jcabi.github.IssueLabels;
import com.jcabi.github.Repo;
import java.io.IOException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.json.JsonObject;

/**
 * Job in GitHub.
 *
 * @since 1.0
 */
public final class Job {

    /**
     * Pattern to match.
     */
    private static final Pattern PTN = Pattern.compile(
        "gh:([a-z0-9\\-.]+/[a-z0-9\\-.]+)#(\\d+)"
    );

    /**
     * Issue.
     */
    private final com.jcabi.github.Issue issue;

    /**
     * Ctor.
     * @param iss Issue
     */
    public Job(final com.jcabi.github.Issue iss) {
        this.issue = iss;
    }

    @Override
    public String toString() {
        return String.format(
            "gh:%s#%d",
            this.issue.repo().coordinates()
                .toString().toLowerCase(Locale.ENGLISH),
            this.issue.number()
        );
    }

    /**
     * Reverse.
     */
    public static final class Issue implements com.jcabi.github.Issue {
        /**
         * The GitHub.
         */
        private final Github github;
        /**
         * The text presentation of it.
         */
        private final String label;
        /**
         * Ctor.
         * @param ghb Github
         * @param txt Label
         */
        public Issue(final Github ghb, final String txt) {
            this.github = ghb;
            this.label = txt;
        }
        @Override
        public Repo repo() {
            return this.issue().repo();
        }
        @Override
        public int number() {
            return this.issue().number();
        }
        @Override
        public Comments comments() {
            return this.issue().comments();
        }
        @Override
        public IssueLabels labels() {
            return this.issue().labels();
        }
        @Override
        public Iterable<Event> events() throws IOException {
            return this.issue().events();
        }
        @Override
        public boolean exists() throws IOException {
            return this.issue().exists();
        }
        @Override
        public void patch(final JsonObject json) throws IOException {
            this.issue().patch(json);
        }
        @Override
        public JsonObject json() throws IOException {
            return this.issue().json();
        }
        @Override
        public int compareTo(final com.jcabi.github.Issue obj) {
            return this.issue().compareTo(obj);
        }
        /**
         * Make an issue.
         * @return Issue
         */
        private com.jcabi.github.Issue issue() {
            final Matcher matcher = Job.PTN.matcher(this.label);
            if (!matcher.matches()) {
                throw new IllegalArgumentException(
                    String.format(
                        "Invalid GitHub job label: \"%s\"",
                        this.label
                    )
                );
            }
            final Repo repo = this.github.repos().get(
                new Coordinates.Simple(
                    matcher.group(1)
                )
            );
            return repo.issues().get(
                Integer.parseInt(matcher.group(2))
            );
        }
    }
}
