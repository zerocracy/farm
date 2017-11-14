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
package com.zerocracy.farm.reactive;

import com.jcabi.log.Logger;
import com.jcabi.xml.XML;
import com.zerocracy.jstk.Project;
import com.zerocracy.pm.ClaimIn;
import com.zerocracy.pm.Claims;
import java.io.IOException;
import java.util.Iterator;
import org.cactoos.iterable.LengthOf;
import org.cactoos.iterable.Mapped;
import org.cactoos.text.JoinedText;
import org.cactoos.text.SubText;

/**
 * The action that happens in the {@link Flush}.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.10
 */
final class Flush implements Trigger {

    /**
     * The project.
     */
    private final Project project;

    /**
     * List of stakeholders.
     */
    private final Brigade brigade;

    /**
     * Ctor.
     * @param pkt Project
     * @param bgd Brigade
     */
    Flush(final Project pkt, final Brigade bgd) {
        this.project = pkt;
        this.brigade = bgd;
    }

    @Override
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public void flush() throws IOException {
        final Claims claims = new Claims(this.project).bootstrap();
        int total = 0;
        int left = Integer.MAX_VALUE;
        while (true) {
            final int length = new LengthOf(claims.iterate()).value();
            if (length > left) {
                break;
            }
            left = length;
            final Iterator<XML> found = claims.take();
            if (!found.hasNext()) {
                break;
            }
            this.process(found.next(), total);
            ++total;
        }
    }

    /**
     * Process it.
     * @param xml The claim
     * @param idx Position in the queue
     * @throws IOException If fails
     */
    @SuppressWarnings("PMD.PrematureDeclaration")
    private void process(final XML xml, final int idx) throws IOException {
        final long start = System.currentTimeMillis();
        final ClaimIn claim = new ClaimIn(xml);
        final int total = this.brigade.process(this.project, xml);
        final int left = new Claims(this.project).iterate().size();
        if (total == 0 && claim.hasToken()) {
            throw new IllegalStateException(
                String.format(
                    "Failed to process \"%s\"/\"%s\", no stakeholders",
                    claim.type(), claim.token()
                )
            );
        }
        Logger.info(
            this, "Seen #%d:\"%s/%d/%d\" at \"%s\" by %d stk, %[ms]s [%s]",
            idx, claim.type(), claim.cid(), left,
            this.project.pid(),
            total,
            System.currentTimeMillis() - start,
            new JoinedText(
                "; ",
                new Mapped<>(
                    ent -> String.format(
                        "%s=%s", ent.getKey(),
                        // @checkstyle MagicNumber (1 line)
                        new SubText(ent.getValue(), 0, 20).asString()
                    ),
                    claim.params().entrySet()
                )
            ).asString()
        );
    }

}
