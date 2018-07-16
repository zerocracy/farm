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
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import com.zerocracy.Farm;
import com.zerocracy.Item;
import com.zerocracy.Project;
import java.io.IOException;
import org.cactoos.Scalar;
import org.cactoos.scalar.IoCheckedScalar;
import org.cactoos.scalar.SolidScalar;

/**
 * Project in Slack.
 *
 * @since 1.0
 */
public final class SkProject implements Project {

    /**
     * Project.
     */
    private final Scalar<Project> pkt;

    /**
     * Ctor.
     * @param frm Farm
     * @param evt Event
     */
    public SkProject(final Farm frm, final SlackMessagePosted evt) {
        this(frm, evt.getChannel());
    }

    /**
     * Ctor.
     * @param farm Farm
     * @param channel Channel
     */
    public SkProject(final Farm farm, final SlackChannel channel) {
        this.pkt = new SolidScalar<>(
            () -> farm.find(
                String.format(
                    "@id='%s'",
                    channel.getId()
                )
            ).iterator().next()
        );
    }

    @Override
    public String pid() throws IOException {
        return new IoCheckedScalar<>(this.pkt).value().pid();
    }

    @Override
    public Item acq(final String file) throws IOException {
        return new IoCheckedScalar<>(this.pkt).value().acq(file);
    }

}
