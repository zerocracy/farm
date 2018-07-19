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
import com.zerocracy.Project;
import com.zerocracy.SoftException;
import com.zerocracy.Xocument;
import com.zerocracy.cash.Cash;
import com.zerocracy.cash.CashParsingException;
import java.io.IOException;
import org.cactoos.time.DateAsText;
import org.xembly.Directives;

/**
 * Vesting rates.
 *
 * @since 1.0
 */
public final class Vesting {

    /**
     * Project.
     */
    private final Project project;

    /**
     * Ctor.
     * @param pkt Project
     */
    public Vesting(final Project pkt) {
        this.project = pkt;
    }

    /**
     * Bootstrap it.
     * @return Itself
     * @throws IOException If fails
     */
    public Vesting bootstrap() throws IOException {
        try (final Item wbs = this.item()) {
            new Xocument(wbs.path()).bootstrap("pm/cost/vesting");
        }
        return this;
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
                    "Vesting rate for @%s is not set"
                ).say(login)
            );
        }
        try (final Item item = this.item()) {
            return new Cash.S(
                new Xocument(item).xpath(
                    String.format(
                        "/vesting/person[@id='%s']/rate/text()",
                        login
                    )
                ).get(0)
            );
        } catch (final CashParsingException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Set his rate.
     * @param login The GitHub login
     * @param rate The rate to set
     * @throws IOException If fails
     */
    public void rate(final String login, final Cash rate) throws IOException {
        try (final Item item = this.item()) {
            new Xocument(item).modify(
                new Directives()
                    .xpath(
                        String.format(
                            "/vesting[not(person[@id='%s'])]",
                            login
                        )
                    )
                    .add("person")
                    .attr("id", login)
                    .add("created").set(new DateAsText().asString())
                    .xpath(
                        String.format(
                            "/vesting/person[@id='%s']",
                            login
                        )
                    )
                    .addIf("rate")
                    .set(rate)
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
                String.format("/vesting/person[@id='%s']/rate", login)
            ).isEmpty();
        }
    }

    /**
     * The item.
     * @return Item
     * @throws IOException If fails
     */
    private Item item() throws IOException {
        return this.project.acq("vesting.xml");
    }
}
