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
package com.zerocracy.farm.reactive;

import com.jcabi.log.Logger;
import com.jcabi.xml.XML;
import com.zerocracy.Project;
import com.zerocracy.pm.ClaimIn;
import com.zerocracy.pm.Claims;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import org.cactoos.BiFunc;
import org.cactoos.func.IoCheckedBiFunc;
import org.cactoos.iterable.LengthOf;
import org.cactoos.iterable.Mapped;
import org.cactoos.text.JoinedText;
import org.cactoos.text.SubText;
import org.xembly.Directive;
import org.xembly.Directives;

/**
 * The action that happens in the {@link DefaultFlush}.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.10
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
final class DefaultFlush implements Flush {

    /**
     * List of stakeholders.
     */
    private final IoCheckedBiFunc<Project, XML, Integer> brigade;

    /**
     * Ctor.
     * @param bgd Brigade
     */
    DefaultFlush(final BiFunc<Project, XML, Integer> bgd) {
        this.brigade = new IoCheckedBiFunc<>(bgd);
    }

    @Override
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public void exec(final Project project) throws IOException {
        final Claims claims = new Claims(project).bootstrap();
        int total = 0;
        final int left = new LengthOf(claims.iterate()).intValue();
        for (int idx = 0; idx < left; ++idx) {
            final Iterator<XML> found = claims.take();
            if (!found.hasNext()) {
                break;
            }
            this.process(project, found.next(), total);
            ++total;
        }
    }

    @Override
    public void close() {
        // nothing
    }

    @Override
    public Iterable<Directive> value() {
        return new Directives();
    }

    /**
     * Process it.
     * @param project The project
     * @param xml The claim
     * @param idx Position in the queue
     * @throws IOException If fails
     */
    @SuppressWarnings("PMD.PrematureDeclaration")
    private void process(final Project project, final XML xml,
        final int idx) throws IOException {
        final long start = System.currentTimeMillis();
        final ClaimIn claim = new ClaimIn(xml);
        final int total = this.brigade.apply(project, xml);
        final int left = new Claims(project).iterate().size();
        if (total == 0 && claim.hasToken()) {
            throw new IllegalStateException(
                String.format(
                    "Failed to process \"%s\"/\"%s\", no stakeholders",
                    claim.type(), claim.token()
                )
            );
        }
        Logger.info(
            this,
            "Seen #%d:\"%s/%d/%d\" at \"%s\" by %d stk, %[ms]s [%s]%s",
            idx, claim.type(), claim.cid(), left,
            project.pid(),
            total,
            System.currentTimeMillis() - start,
            new JoinedText(
                "; ",
                new Mapped<Map.Entry<String, String>, String>(
                    ent -> String.format(
                        "%s=%s", ent.getKey(),
                        // @checkstyle MagicNumber (1 line)
                        new SubText(ent.getValue(), 0, 120).asString()
                    ),
                    claim.params().entrySet()
                )
            ).asString(),
            // @checkstyle AvoidInlineConditionalsCheck (1 line)
            claim.hasAuthor() ? String.format(", by @%s", claim.author()) : ""
        );
    }

}
