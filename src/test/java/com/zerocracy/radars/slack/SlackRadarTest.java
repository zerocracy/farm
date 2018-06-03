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
package com.zerocracy.radars.slack;

import com.ullink.slack.simpleslackapi.SlackPersona;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.SlackTeam;
import com.zerocracy.Farm;
import com.zerocracy.farm.fake.FkFarm;
import com.zerocracy.farm.props.PropsFarm;
import com.zerocracy.pmo.Bots;
import com.zerocracy.pmo.Pmo;
import java.io.IOException;
import javax.json.Json;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Tests for {@link SlackRadar}.
 *
 * @author Krzysztof Krason (Krzysztof.Krason@gmail.com)
 * @version $Id$
 * @since 0.1
 * @todo #687:30min There are no tests for the refresh method (and as a result
 *  start method also) of SlackRadar class. Please add tests verifying behavior
 *  of those methods.
 * @checkstyle JavadocMethodCheck (500 lines)
 */
public final class SlackRadarTest {

    @Test
    @SuppressWarnings("unchecked")
    public void closesSession() throws IOException {
        final Farm farm = new PropsFarm(new FkFarm());
        SlackRadarTest.registerBots(farm);
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

    private static SlackSession mockSession() {
        final SlackSession session = Mockito.mock(SlackSession.class);
        Mockito.when(session.sessionPersona())
            .thenReturn(Mockito.mock(SlackPersona.class));
        Mockito.when(session.getTeam())
            .thenReturn(Mockito.mock(SlackTeam.class));
        return session;
    }

    private static void registerBots(final Farm farm) throws IOException {
        final Bots bots = new Bots(new Pmo(farm)).bootstrap();
        bots.register(
            Json.createReader(
                SlackRadar.class.getResourceAsStream("slack-bot.json")
            ).readObject()
        );
    }
}
