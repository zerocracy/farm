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
package com.zerocracy.entry;

import com.jcabi.log.Logger;
import com.zerocracy.Farm;
import com.zerocracy.farm.props.Props;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.cactoos.Scalar;
import org.cactoos.func.SolidFunc;
import org.cactoos.func.UncheckedFunc;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;

/**
 * Twitter client.
 * @since 1.0
 */
public final class ExtTwitter implements Scalar<ExtTwitter.Tweets> {

    /**
     * Singleton.
     */
    private static final UncheckedFunc<Farm, ExtTwitter.Tweets> SINGLETON =
        new UncheckedFunc<>(
            new SolidFunc<>(
                frm -> {
                    final Props props = new Props(frm);
                    final ExtTwitter.Tweets twitter;
                    if (props.has("//testing")) {
                        twitter = new ExtTwitter.MkTweets();
                    } else {
                        twitter = ExtTwitter.prod(props);
                    }
                    return twitter;
                }
            )
        );

    /**
     * Farm.
     */
    private final Farm farm;
    /**
     * Ctor.
     * @param frm Farm
     */
    public ExtTwitter(final Farm frm) {
        this.farm = frm;
    }

    @Override
    public ExtTwitter.Tweets value() {
        return ExtTwitter.SINGLETON.apply(this.farm);
    }

    /**
     * Production tweets.
     * @param props Properties
     * @return Tweets
     * @throws IOException If fails
     */
    private static ExtTwitter.Tweets prod(final Props props)
        throws IOException {
        final Twitter twitter = new TwitterFactory().getInstance();
        twitter.setOAuthConsumer(
            props.get("//twitter/key"),
            props.get("//twitter/secret")
        );
        twitter.setOAuthAccessToken(
            new AccessToken(
                props.get("//twitter/token"),
                props.get("//twitter/tsecret")
            )
        );
        return new ExtTwitter.ProdTweets(twitter);
    }

    /**
     * Tweets.
     */
    public interface Tweets {
        /**
         * Publish a tweet.
         * @param text Tweet text
         * @return Tweet id
         * @throws IOException If fails
         */
        long publish(String text) throws IOException;
    }

    /**
     * Tweets that can retrieve tweeted messages.
     */
    public interface Retrievable extends Tweets {
        /**
         * Get list of tweets.
         * @return List of tweets
         */
        List<String> tweets();
    }

    /**
     * Twitter api tweets.
     */
    private static final class ProdTweets implements ExtTwitter.Tweets {
        /**
         * Twitter API.
         */
        private final Twitter api;
        /**
         * Ctor.
         * @param api Twitter API
         */
        private ProdTweets(final Twitter api) {
            this.api = api;
        }

        @Override
        public long publish(final String text) throws IOException {
            try {
                return this.api.updateStatus(text).getId();
            } catch (final TwitterException err) {
                throw new IOException(err);
            }
        }
    }

    /**
     * Test tweets.
     */
    private static final class MkTweets implements ExtTwitter.Retrievable {
        /**
         * Published tweets.
         */
        private final List<String> messages = new CopyOnWriteArrayList<>();

        @Override
        public long publish(final String text) {
            this.messages.add(text);
            Logger.debug(this, "tweet: %s", text);
            return 0L;
        }

        @Override
        public List<String> tweets() {
            return this.messages;
        }
    }
}
