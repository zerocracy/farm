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
package com.zerocracy.radars.github;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.jcabi.dynamo.Attributes;
import com.jcabi.dynamo.Item;
import com.jcabi.dynamo.QueryValve;
import com.jcabi.dynamo.Table;
import com.jcabi.github.Coordinates;
import com.jcabi.github.Github;
import com.jcabi.github.Repo;
import com.zerocracy.Farm;
import com.zerocracy.entry.ExtTwitter;
import java.io.IOException;
import java.util.Iterator;
import javax.json.JsonObject;

/**
 * Tweet about a new repo.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.10
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class RbTweet implements Rebound {

    /**
     * Hash of Dynamo table.
     */
    private static final String HASH = "repo";

    /**
     * Tweet ID in Dynamo table.
     */
    private static final String ATTR = "tweet";

    /**
     * Dynamo table with all seen repos.
     */
    private final Table table;

    /**
     * Twitter client.
     */
    private final transient ExtTwitter.Tweets tweets;

    /**
     * Ctor.
     * @param tbl Dynamo table
     * @param tweets Twitter tweets
     * @checkstyle ParameterNumberCheck (5 lines)
     */
    public RbTweet(final Table tbl, final ExtTwitter.Tweets tweets) {
        this.table = tbl;
        this.tweets = tweets;
    }

    @Override
    public String react(final Farm farm, final Github github,
        final JsonObject event) throws IOException {
        final String field = "repository";
        final String answer;
        if (event.containsKey(field)) {
            final Repo repo = github.repos().get(
                new Coordinates.Simple(
                    event.getJsonObject(field).getString("full_name")
                )
            );
            if (new Repo.Smart(repo).isPrivate()) {
                answer = String.format(
                    "%s is private, won't tweet", repo.coordinates()
                );
            } else {
                answer = this.send(repo);
            }
        } else {
            answer = "Repo is not visible";
        }
        return answer;
    }

    /**
     * Send tweet for this repo.
     * @param repo The repo
     * @return The answer
     * @throws IOException If fails
     */
    private String send(final Repo repo) throws IOException {
        final String answer;
        long tweet = this.tweet(repo);
        if (tweet == 0L) {
            tweet = this.tweets.publish(
                String.format(
                    "We started to work with https://github.com/%s",
                    repo.coordinates()
                )
            );
            this.mark(repo, tweet);
            answer = String.format(
                "See https://twitter.com/0crat/success/%d", tweet
            );
        } else {
            answer = String.format(
                "Tweeted earlier: https://twitter.com/0crat/success/%d",
                tweet
            );
        }
        return answer;
    }

    /**
     * We tweeted about this repo already?
     * @param repo The repo
     * @return Tweet ID or zero
     * @throws IOException If fails
     */
    private long tweet(final Repo repo) throws IOException {
        final Iterator<Item> items = this.table
            .frame()
            .through(new QueryValve().withLimit(1))
            .where(RbTweet.HASH, repo.coordinates().toString())
            .iterator();
        final long tweet;
        if (items.hasNext()) {
            tweet = Long.parseLong(
                items.next().get(RbTweet.ATTR).getN()
            );
        } else {
            tweet = 0L;
        }
        return tweet;
    }

    /**
     * Mark it as tweeted already.
     * @param repo The repo
     * @param tweet Tweet cid
     * @throws IOException If fails
     */
    private void mark(final Repo repo, final long tweet) throws IOException {
        this.table.put(
            new Attributes()
                .with(RbTweet.HASH, repo.coordinates().toString())
                .with(
                    RbTweet.ATTR,
                    new AttributeValue().withN(Long.toString(tweet))
                )
        );
    }
}
