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
package com.zerocracy.pm.cost;

import com.zerocracy.Item;
import com.zerocracy.Project;
import com.zerocracy.Xocument;
import com.zerocracy.cash.Cash;
import com.zerocracy.cash.CashParsingException;
import java.io.IOException;
import org.xembly.Directives;

/**
 * Equity.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.20
 */
public final class Equity {

    /**
     * Project.
     */
    private final Project project;

    /**
     * Ctor.
     * @param pkt Project
     */
    public Equity(final Project pkt) {
        this.project = pkt;
    }

    /**
     * Bootstrap it.
     * @return Itself
     * @throws IOException If fails
     */
    public Equity bootstrap() throws IOException {
        try (final Item wbs = this.item()) {
            new Xocument(wbs.path()).bootstrap("pm/cost/equity");
        }
        return this;
    }

    /**
     * Increase ownership of the user, by adding cash reward.
     * @param login The GitHub login
     * @param value The value to add
     * @throws IOException If fails
     */
    public void add(final String login, final Cash value) throws IOException {
        try (final Item item = this.item()) {
            final Cash cap = new Cash.S(
                new Xocument(item).xpath(
                    "/equity/cap/text()"
                ).get(0)
            );
            final double shares = Double.parseDouble(
                new Xocument(item).xpath(
                    "/equity/shares/text()"
                ).get(0)
            );
            final double inc = value.decimal().doubleValue() * shares
                / cap.decimal().doubleValue();
            new Xocument(item).modify(
                new Directives()
                    .xpath(
                        String.format(
                            "/equity/owners[not(owner[@id='%s'])]", login
                        )
                    )
                    .add("owner").attr("id", login).set("0")
                    .xpath(
                        String.format(
                            "/equity/owners/owner[@id='%s']", login
                        )
                    )
                    .xset(String.format(". + %.4f", inc))
            );
        } catch (final CashParsingException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * The item.
     * @return Item
     * @throws IOException If fails
     */
    private Item item() throws IOException {
        return this.project.acq("equity.xml");
    }
}
