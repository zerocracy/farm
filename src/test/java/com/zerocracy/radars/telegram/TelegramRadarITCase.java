package com.zerocracy.radars.telegram;

import com.jcabi.aspects.Tv;
import com.zerocracy.jstk.fake.FkFarm;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.cactoos.ScalarHasValue;
import org.cactoos.func.And;
import org.cactoos.list.EndlessIterable;
import org.cactoos.list.LimitedIterable;
import org.hamcrest.MatcherAssert;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Integration test for Telegram bot.
 * <p>
 * To test bot radar {@link TelegramRadar} you should replace
 * <code>bot-token</code> and <code>bot-name</code>
 * with actual bot name and token.<br/>
 * After run send a message to your bot within fifty seconds.
 * <p>
 * Known issues:
 * <ol>
 * <li>Don't connect to your bot simultaneously from multiple clients,
 * it fails this test</li>
 * </ol>
 * @author Kirill (g4s8.public@gmail.com)
 * @version $Id$
 * @since 0.15
 */
public final class TelegramRadarITCase {

    @Test
    @Ignore
    public void connectToTelegramTest() throws Exception {
        final Map<Long, TmSession> sessions = new ConcurrentHashMap<>();
        try (
            final TelegramRadar rdr = new TelegramRadar(
                new FkFarm(),
                sessions
            )
        ) {
            rdr.start(
                "--bot-token--",
                "--bot--name--"
            );
            MatcherAssert.assertThat(
                new And(
                    new LimitedIterable<>(
                        new EndlessIterable<>(1),
                        Tv.FIFTY
                    ),
                    x -> {
                        TimeUnit.SECONDS.sleep(1L);
                        return sessions.isEmpty();
                    }
                ),
                new ScalarHasValue<>(false)
            );
        }
    }
}
