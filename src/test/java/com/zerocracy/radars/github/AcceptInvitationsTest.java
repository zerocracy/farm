package com.zerocracy.radars.github;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import javax.json.Json;
import javax.json.JsonObject;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.Test;
import org.mockito.Mockito;

import com.jcabi.github.Github;
import com.jcabi.github.Limit;
import com.jcabi.github.Limits;

/**
 * Test case for {@link AcceptInvitations}.
 *
 */
public final class AcceptInvitationsTest {
    /**
     * Test log appender.
     */
    private final static class TestAppender extends AppenderSkeleton {
        /**
         * Log messages.
         */
        private final List<String> log = new Vector<>();

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
    
	/**
	 * Tests that when {@link AcceptInvitations#exec(Boolean)} is called on a github
	 * with quota exceeded a warn log is generated.
	 * @see Quota#over()
	 */
	@Test
	public void quotaExceeded() throws Exception {
		final Github github = Mockito.mock(Github.class);
		
		final Limits exceededLimits = Mockito.mock(Limits.class);
		Mockito.doReturn(new Limit() {
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
		}).when(exceededLimits).get(Limits.CORE);
		
		Mockito.doReturn(exceededLimits).when(github).limits();
		
		final TestAppender testAppender = new TestAppender();
        final Logger logger = Logger.getRootLogger();
        logger.addAppender(testAppender);
		try
		{
			new AcceptInvitations(github).exec(Boolean.TRUE);
		}
		finally
		{
			logger.removeAppender(testAppender);
		}

		assertEquals(Arrays.asList("GitHub API is over quota. Cancelling AcceptInvitations execution."),
				testAppender.log);
	}
}
