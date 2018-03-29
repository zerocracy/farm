/**
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
package com.zerocracy.pm;

import com.jcabi.xml.ClasspathSources;
import com.jcabi.xml.XMLDocument;
import com.jcabi.xml.XSLChain;
import com.jcabi.xml.XSLDocument;
import com.zerocracy.Farm;
import com.zerocracy.Project;
import com.zerocracy.pmo.Pmo;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import org.cactoos.collection.Mapped;
import org.cactoos.time.DateAsText;
import org.cactoos.time.ZonedDateTimeAsText;
import org.xembly.Directive;
import org.xembly.Directives;
import org.xembly.Xembler;

/**
 * Claim.
 *
 * <p>Objects of this class are immutable.</p>
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.9
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
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
                .add("created").set(new DateAsText().asString())
                .up()
        );
    }

    /**
     * Ctor.
     * @param list List of dirs, if any
     */
    ClaimOut(final Iterable<Directive> list) {
        this.dirs = new Directives(list);
    }

    /**
     * Post it to the project.
     * @param project Project
     * @throws IOException If fails
     */
    @SuppressWarnings("overloads")
    public void postTo(final Project project) throws IOException {
        new Claims(project).bootstrap().add(
            new XSLChain(
                new Mapped<>(
                    s -> XSLDocument.make(
                        ClaimOut.class.getResource(
                            String.format("post-claim-out/%s.xsl", s)
                        )
                    ),
                    "me-into-login",
                    "normalize-login",
                    "normalize-minutes",
                    "validate-login",
                    "validate-job",
                    "validate-role",
                    "validate-cause",
                    "prohibit-param-names",
                    "prohibit-duplicated-flow"
                )
            )
                .with(new ClasspathSources())
                .transform(new XMLDocument(new Xembler(this).xmlQuietly()))
        );
    }

    /**
     * Post it to the PMO.
     * @param farm The farm
     * @throws IOException If fails
     */
    @SuppressWarnings("overloads")
    public void postTo(final Farm farm) throws IOException {
        this.postTo(new Pmo(farm));
    }

    /**
     * With this type.
     * @param type The type
     * @return This
     */
    public ClaimOut type(final String type) {
        return new ClaimOut(
            this.dirs
                .push()
                .xpath("type")
                .remove()
                .pop()
                .add("type").set(type).up()
        );
    }

    /**
     * With this token.
     * @param token The token
     * @return This
     */
    public ClaimOut token(final Object token) {
        return new ClaimOut(
            this.dirs
                .push()
                .xpath("token")
                .remove()
                .pop()
                .add("token").set(token).up()
        );
    }

    /**
     * With this claim ID.
     * @param cid Claim ID
     * @return This
     */
    public ClaimOut cid(final long cid) {
        return new ClaimOut(
            this.dirs
                .push()
                .xpath(".")
                .attr("id", cid)
                .pop()
        );
    }

    /**
     * With this author.
     * @param author GitHub login of the author
     * @return This
     */
    public ClaimOut author(final Object author) {
        final String login = author.toString().toLowerCase(Locale.ENGLISH);
        if ("0crat".equals(login)) {
            throw new IllegalArgumentException(
                "0crat can't be the author of a claim"
            );
        }
        return new ClaimOut(
            this.dirs
                .push()
                .xpath("author")
                .remove()
                .pop()
                .add("author").set(author).up()
        );
    }

    /**
     * Until this amount of seconds.
     * @param seconds The amount of seconds to wait
     * @return This
     */
    public ClaimOut until(final long seconds) {
        return new ClaimOut(
            this.dirs
                .push()
                .xpath("until")
                .remove()
                .pop()
                .add("until")
                .set(
                    new ZonedDateTimeAsText(
                        ZonedDateTime.now().plusSeconds(seconds)
                    ).asString()
                )
                .up()
        );
    }

    /**
     * With this param.
     * @param name Name
     * @param value Value
     * @return This
     */
    public ClaimOut param(final String name, final Object value) {
        if (name == null) {
            throw new IllegalArgumentException(
                "Name can't be NULL"
            );
        }
        if (value == null) {
            throw new IllegalArgumentException(
                String.format("Value can't be NULL for \"%s\"", name)
            );
        }
        return new ClaimOut(
            this.dirs
                .addIf("params")
                .push()
                .xpath(String.format("param[@name='%s']", name))
                .remove()
                .pop()
                .add("param").attr("name", name).set(value).up()
                .up()
        );
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

}
