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

import java.io.IOException;
import org.cactoos.Text;
import org.cactoos.text.TextOf;
import org.cactoos.text.TrimmedText;
import org.cactoos.text.UncheckedText;

/**
 * Slack direct message.
 * @author Kirill (g4s8.public@gmail.com)
 * @version $Id$
 * @since 0.13
 */
final class DirectMessage implements Text {

    /**
     * Slack message.
     */
    private final Text msg;

    /**
     * Ctor.
     * @param msg Slack message string
     */
    DirectMessage(final String msg) {
        this(new TextOf(msg));
    }

    /**
     * Primary ctor.
     * @param msg Slack message text
     */
    DirectMessage(final Text msg) {
        this.msg = new TrimmedText(msg);
    }

    @Override
    public String asString() throws IOException {
        return this.msg.asString().replaceFirst(
            "^@[a-z0-9][a-z0-9._-]+\\s",
            ""
        );
    }

    @Override
    public int compareTo(final Text text) {
        return new UncheckedText(this).compareTo(text);
    }

    @Override
    public String toString() {
        return new UncheckedText(this).asString();
    }
}
