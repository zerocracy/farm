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

import com.jcabi.xml.XML;
import com.zerocracy.jstk.Project;
import com.zerocracy.jstk.Stakeholder;
import com.zerocracy.pm.ClaimIn;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.cactoos.iterable.Mapped;
import org.cactoos.list.ListOf;
import org.cactoos.list.StickyList;

/**
 * Pool of stakeholders.
 * @author Kirill (g4s8.public@gmail.com)
 * @version $Id$
 * @since 0.16.1
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)B
 */
public final class StkPool {

    /**
     * Initial pool size.
     */
    private static final int SIZE_INIT = 50;

    /**
     * Learned stakeholders by claim criteria.
     */
    private final Map<StkCriteria, List<Stakeholder>> learned;

    /**
     * Raw stakeholders.
     */
    private final Iterable<Stakeholder> raw;

    /**
     * Ctor.
     * @param stakeholders Raw stakeholders
     */
    public StkPool(final Iterable<Stakeholder> stakeholders) {
        this.raw = stakeholders;
        this.learned = new HashMap<>(StkPool.SIZE_INIT);
    }

    /**
     * Stakeholders for project and claim. Must be closed after processing
     * to return them into pull.
     * @param project A project
     * @param claim Claim
     * @return Stakeholders
     * @throws IOException If fails
     */
    @SuppressWarnings("IOResourceOpenedButNotSafelyClosed")
    public StkPooled stakeholders(final Project project,
        final XML claim) throws IOException {
        final StkCriteria criteria = new StkCriteria(
            project.pid(),
            new ClaimIn(claim).type()
        );
        final StkPooled pooled;
        synchronized (this.learned) {
            if (this.learned.containsKey(criteria)) {
                pooled = new StkPooled.Simple(this.learned.get(criteria));
            } else {
                pooled = new StkLearning(
                    new StickyList<>(
                        new ListOf<StkWithStatus>(
                            new Mapped<>(
                                this.raw,
                                StkWithStatus::new
                            )
                        )
                    ),
                    this.learned,
                    criteria
                );
            }
        }
        return pooled;
    }

}
