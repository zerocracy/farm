/**
 * Copyright (c) 2016 Zerocracy
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
package com.zerocracy.crews.slack;

import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import com.zerocracy.jstk.Farm;
import com.zerocracy.jstk.Item;
import com.zerocracy.jstk.Project;
import java.io.IOException;

/**
 * Project in Slack.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.1
 */
public final class SkProject implements Project {

    /**
     * Farm.
     */
    private final Farm farm;

    /**
     * Event.
     */
    private final SlackMessagePosted event;

    /**
     * Ctor.
     * @param frm Farm
     * @param evt Event
     */
    public SkProject(final Farm frm, final SlackMessagePosted evt) {
        this.farm = frm;
        this.event = evt;
    }

    @Override
    public Item acq(final String file) throws IOException {
        return this.project().acq(file);
    }

    /**
     * Make it.
     * @return Project
     * @throws IOException If fails
     */
    private Project project() throws IOException {
        return this.farm.find(
            String.format(
                "id=%s",
                this.event.getChannel().getId()
            )
        ).iterator().next();
    }

}
