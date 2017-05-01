/**
 * Copyright (c) 2016-2017 Zerocracy
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
import com.jcabi.github.Issue;
import com.jcabi.github.IssueLabels;
import com.jcabi.github.Repo;
import java.io.IOException;
import javax.json.JsonObject;

/**
 * An issue in GitHub event.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.10
 */
final class IssueOfEvent implements Issue {

    /**
     * The GitHub.
     */
    private final Github github;

    /**
     * The event.
     */
    private final JsonObject event;

    /**
     * Ctor.
     * @param ghb Github
     * @param evt Event
     */
    IssueOfEvent(final Github ghb, final JsonObject evt) {
        this.github = ghb;
        this.event = evt;
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
    public int compareTo(final Issue issue) {
        return this.issue().compareTo(issue);
    }

    /**
     * Make an issue.
     * @return Issue
     */
    @SuppressWarnings("PMD.AvoidDuplicateLiterals")
    private Issue issue() {
        final int num;
        if (this.event.containsKey("issue")) {
            num = this.event.getJsonObject("issue").getInt("number");
        } else if (this.event.containsKey("pull_request")) {
            num = this.event.getJsonObject("pull_request").getInt("number");
        } else {
            throw new IllegalStateException(
                String.format(
                    "Can't find issue number in %s", this.event
                )
            );
        }
        return new SafeIssue(
            this.github.repos().get(
                new Coordinates.Simple(
                    this.event.getJsonObject("repository")
                        .getString("full_name")
                )
            ).issues().get(num)
        );
    }

}
