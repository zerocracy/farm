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

import com.jcabi.github.Github;
import com.jcabi.github.Limit;
import com.jcabi.github.Limits;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.json.Json;
import javax.json.JsonObject;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test case for {@link AcceptInvitations}.
 * @since 1.0
 */
public final class AcceptInvitationsTest {
    /**
     * Tests that when {@link AcceptInvitations#exec(Boolean)} is called on a
     * github with quota exceeded a warn log is generated.
     * @see Quota#over()
     * @throws Exception if error occurs during test.
     */
    @Test
    public void exceedsQuota() throws Exception {
        final Github github = Mockito.mock(Github.class);
        final Limits limits = Mockito.mock(Limits.class);
        Mockito.doReturn(
            new Limit() {
                @Override
                public Github github() {
                    return github;
                }

                @Override
                public JsonObject json() {
                    return Json.createObjectBuilder()
                        // @checkstyle MagicNumber (2 lines)
                        .add("limit", 5000)
                        .add("remaining", 200)
                        .add("reset", System.currentTimeMillis())
                        .build();
                }
            }
        ).when(limits).get(Limits.CORE);
        Mockito.doReturn(limits).when(github).limits();
        final TestAppender testappender = new TestAppender();
        final Logger logger = Logger.getRootLogger();
        logger.addAppender(testappender);
        try {
            new AcceptInvitations(github).exec(Boolean.TRUE);
        } finally {
            logger.removeAppender(testappender);
        }
        MatcherAssert.assertThat(
            testappender.warns, Matchers.hasItem(
                // @checkstyle LineLength (1 line)
                "GitHub API is over quota. Cancelling AcceptInvitations execution."
            )
        );
    }

    /**
     * Test log appender.
     */
    private static class TestAppender extends AppenderSkeleton {
        /**
         * Warn messages logged.
         */
        private final List<String> warns = new CopyOnWriteArrayList<>();

        @Override
        public boolean requiresLayout() {
            return false;
        }

        @Override
        public void close() {
            // Nothing to do.
        }

        @Override
        protected void append(final LoggingEvent event) {
            if (event.getLevel() == Level.WARN) {
                this.warns.add(event.getMessage().toString());
            }
        }
    }
}
