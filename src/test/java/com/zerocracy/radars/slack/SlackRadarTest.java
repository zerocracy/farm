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
package com.zerocracy.radars.slack;

import com.ullink.slack.simpleslackapi.SlackPersona;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.SlackTeam;
import com.zerocracy.Farm;
import com.zerocracy.farm.fake.FkFarm;
import com.zerocracy.farm.fake.FkProject;
import com.zerocracy.farm.props.PropsFarm;
import com.zerocracy.pmo.Bots;
import com.zerocracy.pmo.Pmo;
import java.io.IOException;
import java.util.UUID;
import javax.json.Json;
import org.cactoos.func.StickyFunc;
import org.cactoos.map.MapEntry;
import org.cactoos.map.MapOf;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Tests for {@link SlackRadar}.
 *
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 * @checkstyle ClassDataAbstractionCoupling (2 lines)
 */
public final class SlackRadarTest {

    @Test
    @SuppressWarnings("unchecked")
    public void closesSession() throws IOException {
        final Farm farm = new PropsFarm(SlackRadarTest.uniqueFarm());
        SlackRadarTest.registerBots(farm, "slack-bot.json");
        final SlackSession session = SlackRadarTest.mockSession();
        final SlackRadar radar = new SlackRadar(
            farm,
            Mockito.mock(Reaction.class),
            sess -> session
        );
        radar.refresh();
        radar.close();
        Mockito.verify(session).disconnect();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void refreshesTokens() throws IOException {
        final Farm farm = new PropsFarm(SlackRadarTest.uniqueFarm());
        SlackRadarTest.registerBots(farm, "slack-bot2.json");
        final String first = "2xoxb-XXXXXXXXXXXX-TTTTTTTTTTTTTT";
        final MapOf<String, SlackSession> sessions = new MapOf<>(
            new MapEntry<>(first, SlackRadarTest.mockSession())
        );
        final SlackRadar radar = new SlackRadar(
            farm,
            Mockito.mock(Reaction.class),
            sessions::get
        );
        radar.refresh();
        Mockito.verify(sessions.get(first)).connect();
    }

    private static SlackSession mockSession() {
        final SlackSession session = Mockito.mock(SlackSession.class);
        Mockito.when(session.sessionPersona())
            .thenReturn(Mockito.mock(SlackPersona.class));
        Mockito.when(session.getTeam())
            .thenReturn(Mockito.mock(SlackTeam.class));
        return session;
    }

    private static void registerBots(final Farm farm, final String file)
        throws IOException {
        final Bots bots = new Bots(new Pmo(farm)).bootstrap();
        bots.register(
            Json.createReader(
                SlackRadar.class.getResourceAsStream(file)
            ).readObject()
        );
    }

    /**
     * Creates a farm with unique identifier.
     * This helps with singleton implementation of ExtSlack which caches the
     * slack sessions based on farm equals/hashcode.
     * @return Unique farm
     */
    private static FkFarm uniqueFarm() {
        return new FkFarm(
            new StickyFunc<>(FkProject::new),
            UUID.randomUUID().toString()
        );
    }
}
