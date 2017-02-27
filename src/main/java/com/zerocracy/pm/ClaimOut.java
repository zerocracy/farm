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
import java.util.Iterator;
import java.util.Map;
import org.xembly.Directive;
import org.xembly.Directives;

/**
 * Claim.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.9
 */
public final class ClaimOut implements Iterable<Directive> {

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
                .attr("id", System.nanoTime() % (long) Integer.MAX_VALUE)
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
     */
    public void postTo(final Project project) throws IOException {
        try (final Claims claims = new Claims(project).lock()) {
            claims.add(this);
        }
    }

    /**
     * With this type.
     * @param type The type
     * @return This
     */
    public ClaimOut type(final String type) {
        this.dirs.add("type").set(type).up();
        return this;
    }

    /**
     * With this token.
     * @param token The token
     * @return This
     */
    public ClaimOut token(final Object token) {
        this.dirs.add("token").set(token).up();
        return this;
    }

    /**
     * With this author.
     * @param author GitHub login of the author
     * @return This
     */
    public ClaimOut author(final Object author) {
        this.dirs.add("author").set(author).up();
        return this;
    }

    /**
     * With this param.
     * @param name Name
     * @param value Value
     * @return This
     */
    public ClaimOut param(final String name, final Object value) {
        this.dirs.addIf("params")
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
            final String type;
            if (this.token.startsWith("slack;")) {
                type = "notify in slack";
            } else if (this.token.startsWith("github;")) {
                type = "notify in github";
            } else {
                throw new IllegalArgumentException(
                    String.format("Unknown token: \"%s\"", this.token)
                );
            }
            return new ClaimOut()
                .type(type)
                .token(this.token)
                .param("message", this.msg)
                .iterator();
        }
    }

    /**
     * Message to user.
     */
    public static final class ToUser implements Iterable<Directive> {
        /**
         * Project.
         */
        private final Project project;
        /**
         * Login.
         */
        private final String login;
        /**
         * Message.
         */
        private final String msg;
        /**
         * Ctor.
         * @param pkt Project
         * @param user GitHub login
         * @param message Message
         */
        public ToUser(final Project pkt, final String user,
            final String message) {
            this.project = pkt;
            this.login = user;
            this.msg = message;
        }
        @Override
        public Iterator<Directive> iterator() {
            return new ClaimOut.Notify(
                String.format(
                    "slack;%s;%s;direct",
                    this.project, this.login
                ),
                this.msg
            ).iterator();
        }
    }

    /**
     * Message to project.
     */
    public static final class ToProject implements Iterable<Directive> {
        /**
         * Project.
         */
        private final Project project;
        /**
         * Message.
         */
        private final String msg;
        /**
         * Ctor.
         * @param pkt Project
         * @param message Message
         */
        public ToProject(final Project pkt, final String message) {
            this.project = pkt;
            this.msg = message;
        }
        @Override
        public Iterator<Directive> iterator() {
            return new ClaimOut.Notify(
                String.format("slack;%s", this.project),
                this.msg
            ).iterator();
        }
    }

}
