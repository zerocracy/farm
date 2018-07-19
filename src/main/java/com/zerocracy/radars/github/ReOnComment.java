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

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.jcabi.dynamo.Attributes;
import com.jcabi.dynamo.Item;
import com.jcabi.dynamo.QueryValve;
import com.jcabi.dynamo.Table;
import com.jcabi.github.Comment;
import com.jcabi.github.Coordinates;
import com.jcabi.github.Github;
import com.jcabi.github.Issue;
import com.jcabi.github.Repo;
import com.jcabi.github.Smarts;
import com.jcabi.log.Logger;
import com.zerocracy.Farm;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import javax.json.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.cactoos.iterable.Filtered;
import org.cactoos.iterable.LengthOf;
import org.cactoos.iterable.Mapped;

/**
 * Reaction on GitHub comment.
 *
 * <p>This is what is coming in the JSON:
 * {@see http://developer.github.com/v3/activity/notifications/#list-your-notifications-in-a-repository}.</p>
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class ReOnComment implements Reaction {

    /**
     * Hash of Dynamo table.
     */
    private static final String HASH = "issue";

    /**
     * Dynamo attribute with the date of the most recently seen comment.
     */
    private static final String DATE = "since";

    /**
     * Dynamo attribute with the cid of a comment already seen.
     */
    private static final String NUMBER = "comment-seen";

    /**
     * GitHub client.
     */
    private final Github github;

    /**
     * Response.
     */
    private final Response response;

    /**
     * Dynamo table.
     */
    private final Table table;

    /**
     * Ctor.
     * @param ghb Github client
     * @param rsp Response
     * @param tbl Table
     */
    public ReOnComment(final Github ghb, final Response rsp, final Table tbl) {
        this.github = ghb;
        this.response = rsp;
        this.table = tbl;
    }

    @Override
    public String react(final Farm farm, final JsonObject event)
        throws IOException {
        final JsonObject subject = event.getJsonObject("subject");
        final String type = subject.getString("type");
        final String result;
        if ("Issue".equalsIgnoreCase(type)
            || "PullRequest".equalsIgnoreCase(type)) {
            final Repo repo = this.github.repos().get(
                new Coordinates.Simple(
                    event.getJsonObject("repository").getString("full_name")
                )
            );
            final Issue issue = repo.issues().get(
                Integer.parseInt(
                    StringUtils.substringAfterLast(
                        subject.getString("url"),
                        "/"
                    )
                )
            );
            final long start = System.currentTimeMillis();
            final Iterable<Comment.Smart> comments = new Smarts<>(
                this.comments(issue)
            );
            for (final Comment.Smart comment : comments) {
                this.send(farm, comment);
            }
            Logger.info(
                this, "%d comments found in %s and processed in %[ms]s: %s",
                new LengthOf(comments).intValue(),
                issue.repo().coordinates(),
                System.currentTimeMillis() - start,
                String.join(
                    ", ",
                    new Mapped<Comment.Smart, String>(
                        cmt -> String.format("#%d", cmt.number()),
                        comments
                    )
                )
            );
            result = String.format("%s#%s", repo.coordinates(), issue.number());
        } else {
            result = String.format("Comment to \"%s\" ignored", type);
        }
        return result;
    }

    /**
     * Fetch most recent not-yet-seen comments.
     * @param issue The issue
     * @return Comments
     * @throws IOException If fails
     */
    private Iterable<Comment> comments(final Issue issue) throws IOException {
        final Iterator<Item> items = this.table
            .frame()
            .through(new QueryValve().withLimit(1))
            .where(ReOnComment.HASH, ReOnComment.name(issue))
            .iterator();
        final long since;
        final int seen;
        if (items.hasNext()) {
            final Item item = items.next();
            since = Long.parseLong(item.get(ReOnComment.DATE).getN());
            seen = Integer.parseInt(item.get(ReOnComment.NUMBER).getN());
        } else {
            since = 0L;
            // @checkstyle MagicNumber (1 line)
            seen = 276041068;
        }
        return new Filtered<>(
            comment -> comment.number() > seen,
            new Mapped<>(
                SafeComment::new,
                issue.comments().iterate(new Date(since))
            )
        );
    }

    /**
     * Send this comment through.
     * @param farm Farm
     * @param comment The comment
     * @throws IOException If fails
     */
    private void send(final Farm farm, final Comment.Smart comment)
        throws IOException {
        this.table.put(
            new Attributes()
                .with(ReOnComment.HASH, ReOnComment.name(comment.issue()))
                .with(
                    ReOnComment.DATE,
                    new AttributeValue().withN(
                        Long.toString(comment.createdAt().getTime())
                    )
                )
                .with(
                    ReOnComment.NUMBER,
                    new AttributeValue().withN(
                        Integer.toString(comment.number())
                    )
                )
        );
        final String author = comment.author()
            .login().toLowerCase(Locale.ENGLISH);
        final String self = comment.issue().repo().github()
            .users().self().login().toLowerCase(Locale.ENGLISH);
        if (!author.equals(self)) {
            this.response.react(farm, comment);
        }
    }

    /**
     * Create issue name.
     * @param issue The name
     * @return Name
     */
    private static String name(final Issue issue) {
        return String.format(
            "%s#%d",
            issue.repo().coordinates(),
            issue.number()
        );
    }

}
