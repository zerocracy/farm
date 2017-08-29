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
import org.cactoos.Proc;

/**
 * The action that happens in the {@link Flush}.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.10
 */
final class FlushAction implements Proc<Boolean> {

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
    FlushAction(final Project pkt, final Brigade bgd) {
        this.project = pkt;
        this.brigade = bgd;
    }

    @Override
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public void exec(final Boolean input) throws Exception {
        while (true) {
            final Iterator<XML> found;
            try (final Claims claims = new Claims(this.project).lock()) {
                found = claims.iterate().iterator();
            }
            boolean more = false;
            if (found.hasNext()) {
                this.process(found.next());
                more = true;
            }
            if (!more) {
                break;
            }
        }
    }

    /**
     * Process it.
     * @param xml The claim
     * @throws IOException If fails
     */
    @SuppressWarnings("PMD.PrematureDeclaration")
    private void process(final XML xml) throws IOException {
        final long start = System.currentTimeMillis();
        final ClaimIn claim = new ClaimIn(xml);
        final int total = this.brigade.process(this.project, xml);
        final int left;
        try (final Claims claims = new Claims(this.project).lock()) {
            claims.remove(claim.number());
            left = claims.iterate().size();
        }
        if (total == 0 && claim.hasToken()) {
            throw new IllegalStateException(
                String.format(
                    "Failed to process \"%s\"/\"%s\", no stakeholders",
                    claim.type(), claim.token()
                )
            );
        }
        Logger.info(
            this, "Seen \"%s/%d/%d\" at \"%s\" by %d stk, %[ms]s",
            claim.type(), claim.number(), left,
            this.project.toString(),
            total,
            System.currentTimeMillis() - start
        );
    }

}
