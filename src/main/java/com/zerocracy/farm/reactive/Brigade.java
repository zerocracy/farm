/*
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

import com.jcabi.xml.XML;
import com.zerocracy.Project;
import com.zerocracy.Stakeholder;
import com.zerocracy.farm.MismatchException;
import java.io.IOException;
import org.cactoos.BiFunc;
import org.cactoos.iterable.Filtered;
import org.cactoos.iterable.IterableOf;
import org.cactoos.iterable.LengthOf;
import org.cactoos.iterable.Mapped;
import org.cactoos.list.SolidList;

/**
 * Brigade of stakeholders.
 * @since 1.0
 */
public final class Brigade implements BiFunc<Project, XML, Integer> {

    /**
     * Stakeholders.
     */
    private final Iterable<Stakeholder> pool;

    /**
     * Ctor.
     * @param list List of stakeholders
     */
    public Brigade(final Stakeholder... list) {
        this(new IterableOf<>(list));
    }

    /**
     * Ctor.
     * @param list List of stakeholders
     */
    public Brigade(final Iterable<Stakeholder> list) {
        this.pool = new SolidList<>(new Mapped<>(StkSmart::new, list));
    }

    @Override
    public Integer apply(final Project project, final XML xml) {
        return new LengthOf(
            new Filtered<>(
                stk -> Brigade.process(stk, project, xml),
                this.pool
            )
        ).intValue();
    }

    /**
     * Process this claim.
     * @param stk Stakeholder
     * @param project Project
     * @param xml XML to process
     * @return TRUE if this one was interested
     * @throws IOException If fails
     */
    private static boolean process(final Stakeholder stk, final Project project,
        final XML xml) throws IOException {
        boolean done;
        try {
            stk.process(project, xml);
            done = true;
        } catch (final MismatchException ex) {
            done = false;
        }
        return done;
    }

}
