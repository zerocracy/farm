/*
 * Copyright (c) 2016-2019 Zerocracy
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
package com.zerocracy.claims.proc;

import com.amazonaws.services.sqs.model.Message;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.cactoos.Proc;
import org.cactoos.scalar.Reduced;
import org.cactoos.scalar.UncheckedScalar;
import org.xembly.Directive;
import org.xembly.Directives;

/**
 * Guts of current proc messages.
 *
 * @since 1.0
 */
public final class ProcGuts implements Proc<Message>, Iterable<Directive> {

    /**
     * Current processing messages.
     */
    private final List<Message> msgs;

    /**
     * Origin.
     */
    private final Proc<Message> origin;

    /**
     * Ctor.
     * @param origin Origin proc
     */
    public ProcGuts(final Proc<Message> origin) {
        this.origin = origin;
        this.msgs = new LinkedList<>();
    }

    @Override
    public void exec(final Message input) throws Exception {
        synchronized (this.msgs) {
            this.msgs.add(input);
        }
        try {
            this.origin.exec(input);
        } finally {
            synchronized (this.msgs) {
                this.msgs.remove(input);
            }
        }
    }

    @Override
    public Iterator<Directive> iterator() {
        final List<Message> copy;
        synchronized (this.msgs) {
            copy = new LinkedList<>(this.msgs);
        }
        return new Directives().add("messages").append(
            new UncheckedScalar<>(
                new Reduced<>(
                    new Directives(),
                    (dirs, msg) -> dirs.add("message")
                        .attr("id", msg.getMessageId())
                        .set(msg.toString())
                        .up(),
                    copy
                )
            ).value()
        ).up().iterator();
    }
}
