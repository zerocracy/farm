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
package com.zerocracy.pm.cost;

import com.zerocracy.Item;
import com.zerocracy.Par;
import com.zerocracy.Policy;
import com.zerocracy.Project;
import com.zerocracy.SoftException;
import com.zerocracy.Xocument;
import com.zerocracy.cash.Cash;
import com.zerocracy.cash.CashParsingException;
import java.io.IOException;
import org.cactoos.time.DateAsText;
import org.xembly.Directives;

/**
 * Rates.
 *
 * @since 1.0
 */
public final class Rates {

    /**
     * Project.
     */
    private final Project project;

    /**
     * Ctor.
     * @param pkt Project
     */
    public Rates(final Project pkt) {
        this.project = pkt;
    }

    /**
     * Bootstrap it.
     * @return Itself
     * @throws IOException If fails
     */
    public Rates bootstrap() throws IOException {
        try (final Item wbs = this.item()) {
            new Xocument(wbs.path()).bootstrap("pm/cost/rates");
        }
        return this;
    }

    /**
     * Set rate of a person.
     * @param login User GitHub login
     * @param rate His rate
     * @throws IOException If fails
     */
    public void set(final String login, final Cash rate) throws IOException {
        final Cash max = new Policy().get("16.max", new Cash.S("$256"));
        if (rate.compareTo(max) > 0) {
            throw new SoftException(
                new Par(
                    "This is too high (%s), we do not work with rates",
                    "higher than %s"
                ).say(rate, max)
            );
        }
        final Cash min = new Policy().get("16.min", Cash.ZERO);
        if (!rate.equals(Cash.ZERO) && rate.compareTo(min) < 0) {
            throw new SoftException(
                new Par(
                    "This is too low (%s), we do not work with rates",
                    "lower than %s"
                ).say(rate, min)
            );
        }
        try (final Item item = this.item()) {
            new Xocument(item).modify(
                new Directives()
                    .xpath(String.format("/rates/person[@id='%s']", login))
                    .remove()
            );
            if (!rate.equals(Cash.ZERO)) {
                new Xocument(item).modify(
                    new Directives()
                        .xpath("/rates")
                        .add("person")
                        .attr("id", login)
                        .add("created").set(new DateAsText().asString()).up()
                        .add("rate")
                        .set(rate)
                );
            }
        }
    }

    /**
     * Get his rate.
     * @param login The GitHub login
     * @return Rate
     * @throws IOException If fails
     */
    public Cash rate(final String login) throws IOException {
        if (!this.exists(login)) {
            throw new SoftException(
                new Par(
                    "Rate for @%s is not set"
                ).say(login)
            );
        }
        try (final Item item = this.item()) {
            return new Cash.S(
                new Xocument(item).xpath(
                    String.format("/rates/person[@id='%s']/rate/text()", login)
                ).get(0)
            );
        } catch (final CashParsingException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * This user has a rate set.
     * @param login The GitHub login
     * @return TRUE if rate is set
     * @throws IOException If fails
     */
    public boolean exists(final String login) throws IOException {
        try (final Item item = this.item()) {
            return !new Xocument(item).nodes(
                String.format("/rates/person[@id='%s']/rate", login)
            ).isEmpty();
        }
    }

    /**
     * The item.
     * @return Item
     * @throws IOException If fails
     */
    private Item item() throws IOException {
        return this.project.acq("rates.xml");
    }
}
