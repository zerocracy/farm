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

import com.zerocracy.jstk.Stakeholder;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.cactoos.iterable.Filtered;
import org.cactoos.iterator.Mapped;
import org.cactoos.list.ListOf;

/**
 * Stakeholders that learn from status.
 * @author Kirill (g4s8.public@gmail.com)
 * @version $Id$
 * @since 0.16.1
 */
public final class StkLearning implements StkPooled {

    /**
     * Source stakeholders that can know self status after process.
     */
    private final List<StkWithStatus> src;

    /**
     * A collection where to put only successful stakeholders.
     */
    private final Map<StkCriteria, List<Stakeholder>> lrn;

    /**
     * Claim criteria.
     */
    private final StkCriteria criteria;

    /**
     * Ctor.
     * @param source Source stakeholders.
     * @param learned Learned output collection.
     * @param criteria Claim criteria
     */
    StkLearning(final List<StkWithStatus> source,
        final Map<StkCriteria, List<Stakeholder>> learned,
        final StkCriteria criteria) {
        this.src = source;
        this.lrn = learned;
        this.criteria = criteria;
    }

    @Override
    public void close() {
        synchronized (this.lrn) {
            this.lrn.put(
                this.criteria,
                new ListOf<>(
                    new org.cactoos.iterable.Mapped<>(
                        new Filtered<>(
                            this.src, StkWithStatus::status
                        ),
                        x -> x
                    )
                )
            );
        }
    }

    @Override
    public Iterator<Stakeholder> iterator() {
        return new Mapped<>(this.src.iterator(), x -> x);
    }
}
