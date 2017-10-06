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
package com.zerocracy.pm;

import com.zerocracy.jstk.Project;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import org.xembly.Directive;
import org.xembly.Directives;

/**
 * Claim.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.9
 */
@SuppressWarnings({ "PMD.AvoidDuplicateLiterals", "PMD.TooManyMethods" })
public final class ClaimOut implements Iterable<Directive> {

    /**
     * Counter of IDs.
     */
    private static final AtomicLong COUNTER = new AtomicLong();

    /**
     * Directives.
     */
    private final Directives dirs;

    /**
     * Ctor.
     */
    public ClaimOut() {
        this(
            new Directives()
                .add("claim")
                .attr("id", ClaimOut.cid())
                .add("created")
                .set(
                    ZonedDateTime.now().format(
                        DateTimeFormatter.ISO_INSTANT
                    )
                )
                .up()
        );
    }

    /**
     * Ctor.
     * @param list List of dirs, if any
     */
    public ClaimOut(final Iterable<Directive> list) {
        this.dirs = new Directives(list);
    }

    /**
     * Post it to the project.
     * @param project Project
     * @throws IOException If fails
     */
    public void postTo(final Project project) throws IOException {
        final Claims claims = new Claims(project).bootstrap();
        claims.add(this);
    }

    /**
     * With this type.
     * @param type The type
     * @return This
     */
    public ClaimOut type(final String type) {
        this.dirs
            .push()
            .xpath("type")
            .remove()
            .pop()
            .add("type").set(type).up();
        return this;
    }

    /**
     * With this token.
     * @param token The token
     * @return This
     */
    public ClaimOut token(final Object token) {
        this.dirs
            .push()
            .xpath("token")
            .remove()
            .pop()
            .add("token").set(token).up();
        return this;
    }

    /**
     * With this claim ID.
     * @param cid Claim ID
     * @return This
     */
    public ClaimOut cid(final long cid) {
        this.dirs
            .push()
            .xpath(".")
            .attr("id", cid)
            .pop();
        return this;
    }

    /**
     * With this author.
     * @param author GitHub login of the author
     * @return This
     */
    public ClaimOut author(final Object author) {
        this.dirs
            .push()
            .xpath("author")
            .remove()
            .pop()
            .add("author").set(author).up();
        return this;
    }

    /**
     * Until this amount of seconds.
     * @param seconds The amount of seconds to wait
     * @return This
     */
    public ClaimOut until(final long seconds) {
        this.dirs
            .push()
            .xpath("until")
            .remove()
            .pop()
            .add("until")
            .set(
                ZonedDateTime.now().plusSeconds(seconds).format(
                    DateTimeFormatter.ISO_INSTANT
                )
            )
            .up();
        return this;
    }

    /**
     * With this param.
     * @param name Name
     * @param value Value
     * @return This
     */
    public ClaimOut param(final String name, final Object value) {
        this.dirs
            .addIf("params")
            .push()
            .xpath(String.format("param[@name='%s']", name))
            .remove()
            .pop()
            .add("param")
            .attr("name", name)
            .set(value)
            .up().up();
        return this;
    }

    /**
     * With these params.
     * @param map Map of params
     * @return This
     */
    public ClaimOut params(final Map<String, String> map) {
        for (final Map.Entry<String, String> entry : map.entrySet()) {
            this.param(entry.getKey(), entry.getValue());
        }
        return this;
    }

    @Override
    public Iterator<Directive> iterator() {
        return new Directives(this.dirs).up().iterator();
    }

    /**
     * Create unique ID.
     * @return ID of the claim
     */
    private static long cid() {
        final long body = Long.parseLong(
            String.format("%1$tj%1$tH%1$tM000", new Date())
        );
        return ClaimOut.COUNTER.incrementAndGet() + body;
    }

    /**
     * Notify.
     */
    public static final class Notify implements Iterable<Directive> {
        /**
         * Token.
         */
        private final String token;
        /**
         * Message.
         */
        private final String msg;
        /**
         * Ctor.
         * @param tkn Token
         * @param message Message
         */
        public Notify(final String tkn, final String message) {
            this.token = tkn;
            this.msg = message;
        }
        @Override
        public Iterator<Directive> iterator() {
            return new ClaimOut()
                .type("Notify")
                .token(this.token)
                .param("message", this.msg)
                .iterator();
        }
    }

}
