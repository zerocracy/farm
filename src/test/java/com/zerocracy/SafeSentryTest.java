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
package com.zerocracy;

import com.zerocracy.sentry.SafeSentry;
import com.zerocracy.sentry.Sentry;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link SafeSentry}.
 * @since 1.0
 * @checkstyle JavadocMethod (500 lines)
 */
public final class SafeSentryTest {

    @Test
    public void logsThrownException() throws Exception {
        final TestAppender appender = new TestAppender();
        final Logger logger = Logger.getRootLogger();
        logger.addAppender(appender);
        final String message = "Exceptional!";
        try {
            new SafeSentry(
                (Sentry) error -> {
                    throw new IllegalStateException(message);
                }
            ).capture(new IllegalStateException("error"));
        } finally {
            logger.removeAppender(appender);
        }
        MatcherAssert.assertThat(
            appender.log,
            Matchers.hasItem(
                Matchers.allOf(
                    Matchers.startsWith("Sentry threw an error"),
                    Matchers.containsString(message)
                )
            )
        );
    }

    /**
     * Test log appender.
     */
    private static class TestAppender extends AppenderSkeleton {
        /**
         * Log messages.
         */
        private final List<String> log = new CopyOnWriteArrayList<>();

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
            this.log.add((String) event.getMessage());
        }
    }
}

