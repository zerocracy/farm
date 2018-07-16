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
import com.zerocracy.Xocument;
import com.zerocracy.cash.Cash;
import com.zerocracy.tools.Latex;
import java.io.IOException;
import java.util.Iterator;
import org.cactoos.Input;
import org.cactoos.io.ResourceOf;
import org.cactoos.text.TextOf;
import org.xembly.Directives;

/**
 * Equity.
 *
 * @since 1.0
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
        try (final Item item = this.item()) {
            new Xocument(item.path()).bootstrap("pm/cost/equity");
        }
        return this;
    }

    /**
     * Get ownership of the user, in PDF.
     * @param login The GitHub login
     * @return PDF document
     * @throws IOException If fails
     */
    public Input pdf(final String login) throws IOException {
        final double share = this.share(login);
        if (share == 0.0d) {
            throw new IllegalArgumentException(
                new Par(
                    "@%s doesn't have any share in %s"
                ).say(login, this.project.pid())
            );
        }
        try (final Item item = this.item()) {
            final Xocument doc = new Xocument(item);
            String latex = new TextOf(
                new ResourceOf("com/zerocracy/pm/cost/equity.tex")
            ).asString();
            final String entity = doc.xpath("//entity/text()", "PROJECT");
            final String ceo = doc.xpath("//ceo/text()", "CEO");
            latex = latex
                .replace("[OWNER]", login)
                .replace("[ENTITY]", entity)
                .replace("[ADDRESS]", doc.xpath("//address/text()", "USA"))
                .replace("[CEO]", ceo)
                .replace("[SHARE]", String.format("%,.6f", share))
                .replace("[SHARES]", String.format("%,.2f", this.shares()))
                .replace("[PAR]", this.par().toString());
            return new Latex(
                latex,
                String.format(
                    "%s; @%s; %s; %s; %s",
                    this.project.pid(), login,
                    entity, ceo,
                    this.ownership(login)
                )
            ).pdf();
        }
    }

    /**
     * Get ownership of the user.
     * @param login The GitHub login
     * @return Ownership of the user
     * @throws IOException If fails
     */
    public String ownership(final String login) throws IOException {
        final double share = this.share(login);
        final String text;
        if (share > 0.0d) {
            final Cash value = this.par().mul((long) share);
            text = String.format(
                "%,.2f shares = %s/%.6f%% of %s",
                // @checkstyle MagicNumber (1 line)
                share, value, share * 100.0d / this.shares(), this.cap()
            );
        } else {
            text = "";
        }
        return text;
    }

    /**
     * Increase ownership of the user, by adding cash reward.
     * @param login The GitHub login
     * @param value The value to add
     * @throws IOException If fails
     */
    public void add(final String login, final Cash value) throws IOException {
        try (final Item item = this.item()) {
            final double inc = value.decimal().doubleValue() * this.shares()
                / this.cap().decimal().doubleValue();
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
                    .xset(String.format(". + %.8f", inc))
            );
        }
    }

    /**
     * Cap.
     * @return Cap
     * @throws IOException If fails
     */
    public Cash cap() throws IOException {
        try (final Item item = this.item()) {
            return new Cash.S(
                new Xocument(item).xpath(
                    "/equity/cap/text()"
                ).get(0)
            );
        }
    }

    /**
     * Shares total.
     * @return Cap
     * @throws IOException If fails
     */
    public double shares() throws IOException {
        try (final Item item = this.item()) {
            return Double.parseDouble(
                new Xocument(item).xpath(
                    "/equity/shares/text()"
                ).get(0)
            );
        }
    }

    /**
     * Get share per the user.
     * @param login The GitHub login
     * @return Shares or zero
     * @throws IOException If fails
     */
    public double share(final String login) throws IOException {
        try (final Item item = this.item()) {
            final Iterator<String> texts = new Xocument(item.path()).xpath(
                String.format(
                    "/equity/owners/owner[@id='%s']/text()", login
                )
            ).iterator();
            double shares = 0.0d;
            if (texts.hasNext()) {
                shares = Double.parseDouble(texts.next());
            }
            return shares;
        }
    }

    /**
     * Par value per share.
     * @return Par value
     * @throws IOException If fails
     */
    public Cash par() throws IOException {
        final double mul = 10000.0d;
        return this.cap().mul((long) mul).div((long) (this.shares() * mul));
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
