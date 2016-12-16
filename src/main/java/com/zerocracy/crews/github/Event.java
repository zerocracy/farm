/**
 * Copyright (c) 2016 Zerocracy
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
package com.zerocracy.crews.github;

import com.jcabi.github.Comment;
import com.jcabi.github.Coordinates;
import com.jcabi.github.Github;
import com.jcabi.github.Issue;
import com.jcabi.github.Repo;
import javax.json.JsonObject;
import org.apache.commons.lang3.StringUtils;

/**
 * GitHub notification event.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.1
 */
public final class Event {

    /**
     * GitHub.
     */
    private final Github github;

    /**
     * GitHub JSON event.
     */
    private final JsonObject json;

    /**
     * Ctor.
     * @param hub Github
     * @param evt Event in JSON
     */
    public Event(final Github hub, final JsonObject evt) {
        this.github = hub;
        this.json = evt;
    }

    @Override
    public String toString() {
        return String.format(
            "\"%s\" #%s at %s: %s",
            this.reason(), this.json.getString("id"),
            this.coordinates(),
            this.json
        );
    }

    /**
     * Reason.
     * @return The reason
     */
    public String reason() {
        return this.json.getString("reason");
    }

    /**
     * Coordinates.
     * @return Coordinates
     */
    public Coordinates coordinates() {
        return new Coordinates.Simple(
            this.json.getJsonObject("repository").getString("full_name")
        );
    }

    /**
     * The comment where it happened.
     * @return Comment
     */
    public Comment comment() {
        final JsonObject subject = this.json.getJsonObject("subject");
        final Repo repo = this.github.repos().get(this.coordinates());
        final Issue issue = repo.issues().get(
            Integer.parseInt(
                StringUtils.substringAfterLast(
                    subject.getString("url"),
                    "/"
                )
            )
        );
        return new Comment.Smart(
            issue.comments().get(
                Integer.parseInt(
                    StringUtils.substringAfterLast(
                        subject.getString("latest_comment_url"),
                        "/"
                    )
                )
            )
        );
    }

}
