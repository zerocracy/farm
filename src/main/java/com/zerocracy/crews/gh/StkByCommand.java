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
package com.zerocracy.crews.gh;

import com.jcabi.github.Comment;
import com.zerocracy.jstk.Stakeholder;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;

/**
 * Stakeholder by command.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.1
 */
public final class StkByCommand implements Stakeholder {

    /**
     * GitHub event.
     */
    private final Event event;

    /**
     * Commands and stakeholders.
     */
    private final Map<String, Stakeholder> routes;

    /**
     * Ctor.
     * @param evt Event in GitHub
     * @param map Routes map
     */
    public StkByCommand(final Event evt, final Map<String, Stakeholder> map) {
        this.event = evt;
        this.routes = map;
    }

    @Override
    public void work() throws IOException {
        final Comment.Smart comment = new Comment.Smart(this.event.comment());
        final String body = comment.body();
        final String[] words = body.trim().split(" ");
        final String command;
        if (words.length > 1) {
            command = words[1];
        } else {
            command = "";
        }
        for (final Map.Entry<String, Stakeholder> entry
            : this.routes.entrySet()) {
            if (command.toLowerCase(Locale.ENGLISH).matches(entry.getKey())) {
                entry.getValue().work();
                break;
            }
        }
    }
}
