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
package com.zerocracy.pmo;

import com.zerocracy.farm.fake.FkProject;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link Agenda}.
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.12
 * @checkstyle JavadocMethodCheck (500 lines)
 */
public final class AgendaTest {

    @Test
    public void addsAndRemovesAgenda() throws Exception {
        final Agenda agenda = new Agenda(new FkProject(), "yegor").bootstrap();
        final String first = "gh:test/test#1";
        agenda.add(first, "REV");
        final String second = "gh:test/test#2";
        agenda.add(second, "QA");
        agenda.remove(first);
        MatcherAssert.assertThat(agenda.jobs(), Matchers.hasItem(second));
    }

    /**
     * Agenda can remove all the orders.
     * @throws Exception If something goes wrong.
     */
    @Test
    public void removesAllOrders() throws Exception {
        final Agenda agenda = new Agenda(new FkProject(), "mihai").bootstrap();
        agenda.add("gh:test2/test#1", "REV1");
        agenda.add("gh:test2/test#2", "QA1");
        agenda.add("gh:test2/test#3", "DEV1");
        MatcherAssert.assertThat(agenda.jobs(), Matchers.not(0));
        agenda.removeAll();
        MatcherAssert.assertThat(agenda.jobs(), Matchers.hasSize(0));
    }

    /**
     * Agenda can remove the only order.
     * @throws Exception If something goes wrong.
     */
    @Test
    public void removesSoleOrder() throws Exception {
        final Agenda agenda = new Agenda(new FkProject(), "john").bootstrap();
        agenda.add("gh:test3/test#1", "REV2");
        MatcherAssert.assertThat(agenda.jobs(), Matchers.hasSize(1));
        agenda.removeAll();
        MatcherAssert.assertThat(agenda.jobs(), Matchers.hasSize(0));
    }

    /**
     * Agenda can "remove" orders if it's empty.
     * @throws Exception If something goes wrong.
     */
    @Test
    public void removesOrdersFromEmptyAgenda() throws Exception {
        final Agenda agenda = new Agenda(new FkProject(), "jane").bootstrap();
        MatcherAssert.assertThat(agenda.jobs(), Matchers.hasSize(0));
        agenda.removeAll();
        MatcherAssert.assertThat(agenda.jobs(), Matchers.hasSize(0));
    }
}
