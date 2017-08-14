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
import com.zerocracy.farm.MismatchException;
import com.zerocracy.jstk.Project;
import com.zerocracy.jstk.Stakeholder;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Brigade of stakeholders.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.10
 */
public final class Brigade {

    /**
     * Full list.
     */
    private final Iterable<Stakeholder> list;

    /**
     * A "cache" of stakeholders.
     */
    private final Map<String, List<Stakeholder>> cache;

    /**
     * Ctor.
     * @param lst List of stakeholders
     */
    public Brigade(final Stakeholder... lst) {
        this(Arrays.asList(lst));
    }

    /**
     * Ctor.
     * @param lst List of stakeholders
     */
    public Brigade(final Iterable<Stakeholder> lst) {
        this.list = lst;
        this.cache = new HashMap<>(0);
    }

    /**
     * Process this claim.
     * @param project Project
     * @param xml XML to process
     * @return How many stakeholders were interested
     * @throws IOException If fails
     */
    public int process(final Project project, final XML xml)
        throws IOException {
        final String key = xml.xpath("/claim/type/text()").get(0);
        this.cache.putIfAbsent(key, new ArrayList<>(0));
        int total = 0;
        for (final Stakeholder stk : this.list) {
            if (this.cache.get(key).contains(stk)) {
                continue;
            }
            if (Brigade.process(stk, project, xml)) {
                ++total;
            } else {
                synchronized (this.cache) {
                    this.cache.get(key).add(stk);
                }
            }
        }
        return total;
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
