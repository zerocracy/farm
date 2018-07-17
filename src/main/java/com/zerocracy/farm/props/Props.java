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
package com.zerocracy.farm.props;

import com.jcabi.xml.XMLDocument;
import com.zerocracy.Farm;
import com.zerocracy.Item;
import com.zerocracy.Project;
import com.zerocracy.pmo.Pmo;
import java.io.IOException;
import java.util.List;
import org.cactoos.text.TextOf;

/**
 * Properties of PMO project.
 *
 * <p>PMO should have a {@code _props.xml} file
 * which specifies properties such as S3 credentials, Paypal or Stripe secrets
 * etc.</p>
 *
 * @since 1.0
 */
public final class Props {

    /**
     * Project.
     */
    private final Project project;

    /**
     * Ctor.
     */
    public Props() {
        this(new PropsFarm());
    }

    /**
     * Ctor.
     * @param farm Original farm
     */
    public Props(final Farm farm) {
        this(new Pmo(farm));
    }

    /**
     * Ctor.
     * @param pkt Project
     */
    public Props(final Project pkt) {
        this.project = pkt;
    }

    @Override
    public String toString() {
        try (final Item item = this.item()) {
            return new TextOf(item.path()).asString();
        } catch (final IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Get one property.
     * @param xpath Xpath to use
     * @return Text
     * @throws IOException If fails
     */
    public String get(final String xpath) throws IOException {
        return this.values(xpath).get(0);
    }

    /**
     * Get one property, with default.
     * @param xpath Xpath to use
     * @param def Default value if it's absent
     * @return Text
     * @throws IOException If fails
     */
    public String get(final String xpath,
        final String def) throws IOException {
        final List<String> list = this.values(xpath);
        final String value;
        if (list.isEmpty()) {
            value = def;
        } else {
            value = list.get(0);
        }
        return value;
    }

    /**
     * Has this property.
     * @param xpath Xpath to use
     * @return TRUE if it exists
     * @throws IOException If fails
     */
    public boolean has(final String xpath) throws IOException {
        return !this.values(xpath).isEmpty();
    }

    /**
     * Get one property, all values.
     * @param xpath Xpath to use
     * @return Texts
     * @throws IOException If fails
     */
    private List<String> values(final String xpath) throws IOException {
        try (final Item item = this.item()) {
            return new XMLDocument(item.path()).xpath(
                String.format("%s/text()", xpath)
            );
        }
    }

    /**
     * Get item.
     * @return The item
     * @throws IOException If fails
     */
    private Item item() throws IOException {
        return this.project.acq("_props.xml");
    }

}
