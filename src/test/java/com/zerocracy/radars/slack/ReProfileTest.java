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

import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackUser;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import com.zerocracy.Farm;
import com.zerocracy.claims.ClaimIn;
import com.zerocracy.claims.ClaimsItem;
import com.zerocracy.farm.props.PropsFarm;
import com.zerocracy.pmo.People;
import com.zerocracy.pmo.Pmo;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test case for {@link ReProfile}.
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class ReProfileTest {

    @Test
    public void parsesValidText() throws Exception {
        final SlackMessagePosted event = Mockito.mock(SlackMessagePosted.class);
        Mockito.doReturn("vacation off").when(event).getMessageContent();
        Mockito.doReturn(Mockito.mock(SlackChannel.class))
            .when(event).getChannel();
        final SlackUser sender = Mockito.mock(SlackUser.class);
        final String sid = "U12345678";
        Mockito.doReturn(sid).when(sender).getId();
        Mockito.doReturn(sender).when(event).getSender();
        final Farm farm = new PropsFarm();
        final People people = new People(farm).bootstrap();
        final String uid = "yegor256";
        people.invite(uid, "mentor");
        people.link(uid, "slack", sid);
        new ReProfile().react(farm, event, null);
        final ClaimIn claim = new ClaimIn(
            new ClaimsItem(new Pmo(farm)).iterate().iterator().next()
        );
        MatcherAssert.assertThat(
            claim.type(),
            Matchers.equalTo("Change vacation mode")
        );
        MatcherAssert.assertThat(
            claim.param("mode"),
            Matchers.equalTo("off")
        );
    }

    @Test
    public void parsesOneWordCommand() throws Exception {
        final SlackMessagePosted event = Mockito.mock(SlackMessagePosted.class);
        Mockito.doReturn("hello").when(event).getMessageContent();
        Mockito.doReturn(Mockito.mock(SlackChannel.class))
            .when(event).getChannel();
        final SlackUser sender = Mockito.mock(SlackUser.class);
        final String sid = "U12345679";
        Mockito.doReturn(sid).when(sender).getId();
        Mockito.doReturn(sender).when(event).getSender();
        final Farm farm = new PropsFarm();
        final People people = new People(farm).bootstrap();
        final String uid = "dmarkov";
        people.invite(uid, "mentor1");
        people.link(uid, "slack", sid);
        new ReProfile().react(farm, event, null);
        final ClaimIn claim = new ClaimIn(
            new ClaimsItem(new Pmo(farm)).iterate().iterator().next()
        );
        MatcherAssert.assertThat(
            claim.type(),
            Matchers.equalTo("Hello profile")
        );
    }

}
