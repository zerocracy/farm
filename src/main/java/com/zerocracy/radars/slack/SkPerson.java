/**
 * Copyright (c) 2016-2017 Zerocracy
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

import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import com.zerocracy.jstk.Farm;
import com.zerocracy.jstk.SoftException;
import com.zerocracy.pmo.People;
import java.io.IOException;
import java.util.Iterator;
import org.takes.misc.Href;

/**
 * Person in Slack.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.1
 */
public final class SkPerson {

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
    public SkPerson(final Farm frm, final SlackMessagePosted evt) {
        this.farm = frm;
        this.event = evt;
    }

    /**
     * User ID.
     * @return User ID
     * @throws IOException If fails
     */
    public String uid() throws IOException {
        final People people = new People(this.farm);
        final String rel = "slack";
        final String href = this.event.getSender().getId();
        final Iterator<String> list = people.find(rel, href).iterator();
        if (!list.hasNext()) {
            throw new SoftException(
                String.join(
                    " ",
                    "I don't know who you are, please click here:",
                    new Href("http://www.0crat.com/alias")
                        .with("rel", rel)
                        .with("href", href)
                )
            );
        }
        return list.next();
    }

}
