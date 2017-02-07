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

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.jcabi.dynamo.Attributes;
import com.jcabi.dynamo.Item;
import com.jcabi.dynamo.QueryValve;
import com.jcabi.dynamo.Table;
import com.jcabi.github.Coordinates;
import com.jcabi.github.Github;
import com.jcabi.github.Repo;
import com.zerocracy.jstk.Farm;
import java.io.IOException;
import java.util.Iterator;
import javax.json.JsonObject;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;

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
    private final transient Twitter twitter;

    /**
     * Ctor.
     * @param tbl Dynamo table
     * @param key Key
     * @param secret Secret
     * @param token Token
     * @param tsecret Token secret
     * @checkstyle ParameterNumberCheck (5 lines)
     */
    public RbTweet(final Table tbl, final String key, final String secret,
        final String token, final String tsecret) {
        this.table = tbl;
        this.twitter = RbTweet.connect(key, secret, token, tsecret);
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
            try {
                tweet = this.twitter.updateStatus(
                    String.format(
                        "We started to work with https://github.com/%s",
                        repo.coordinates()
                    )
                ).getId();
                this.mark(repo, tweet);
                answer = String.format(
                    "See https://twitter.com/0crat/status/%d", tweet
                );
            } catch (final TwitterException ex) {
                throw new IOException(ex);
            }
        } else {
            answer = String.format(
                "Tweeted earlier: https://twitter.com/0crat/status/%d",
                tweet
            );
        }
        return answer;
    }

    /**
     * Connect to Twitter.
     * @param key Key
     * @param secret Secret
     * @param token Token
     * @param tsecret Token secret
     * @return Twitter
     * @checkstyle ParameterNumberCheck (5 lines)
     */
    @SuppressWarnings("PMD.UseObjectForClearerAPI")
    private static Twitter connect(final String key, final String secret,
        final String token, final String tsecret) {
        final TwitterFactory factory = new TwitterFactory();
        final Twitter twitter = factory.getInstance();
        twitter.setOAuthConsumer(key, secret);
        twitter.setOAuthAccessToken(new AccessToken(token, tsecret));
        return twitter;
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
     * @param tweet Tweet number
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
