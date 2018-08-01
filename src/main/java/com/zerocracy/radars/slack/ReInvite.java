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

import com.ullink.slack.simpleslackapi.events.SlackChannelJoined;
import com.zerocracy.Farm;
import com.zerocracy.Par;
import java.io.IOException;

/**
 * Invite to the channel.
 *
 * @since 1.0
 */
final class ReInvite implements Reaction<SlackChannelJoined> {

    @Override
    public boolean react(final Farm farm, final SlackChannelJoined event,
        final SkSession session) throws IOException {
        session.send(
            event.getSlackChannel(),
            new Par(
                "Thanks for inviting me here;",
                "now you have to bootstrap the project, as explained in §12;",
                "project ID is %s"
            ).say(event.getSlackChannel().getId())
        );
        return true;
    }

}
