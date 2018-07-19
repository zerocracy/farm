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
package com.zerocracy;

import com.jcabi.xml.XMLDocument;
import com.zerocracy.cash.Cash;
import com.zerocracy.farm.props.Props;
import com.zerocracy.farm.props.PropsFarm;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import org.cactoos.io.InputOf;
import org.cactoos.text.TextOf;

/**
 * Policy numbers.
 *
 * <p>This class goes to our live policy at www.zerocracy.com/policy.html,
 * finds the number needed inside the HTML, and returns it.
 * If it's a testing mode, the test value is returned.</p>
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class Policy {

    /**
     * Farm.
     */
    private final Farm farm;

    /**
     * Ctor.
     */
    public Policy() {
        this(new PropsFarm());
    }

    /**
     * Ctor.
     * @param frm The farm
     */
    public Policy(final Farm frm) {
        this.farm = frm;
    }

    /**
     * Get value.
     * @param param Parameter of the policy
     * @param test Test value if in testing mode
     * @return Value
     * @throws IOException If fails
     */
    public Cash get(final String param, final Cash test) throws IOException {
        return new Cash.S(this.get(param, test.toString()));
    }

    /**
     * Get value.
     * @param param Parameter of the policy
     * @param test Test value if in testing mode
     * @return Value
     * @throws IOException If fails
     */
    public int get(final String param, final int test) throws IOException {
        return Integer.parseInt(this.get(param, Integer.toString(test)));
    }

    /**
     * Get value.
     * @param param Parameter of the policy
     * @param test Test value if in testing mode
     * @return Value
     * @throws IOException If fails
     */
    public String get(final String param, final String test)
        throws IOException {
        final String result;
        if (new Props(this.farm).has("//testing")) {
            result = test;
        } else {
            final Iterator<String> items = new XMLDocument(
                new TextOf(
                    new InputOf(
                        new URL("http://www.zerocracy.com/policy.html")
                    )
                ).asString()
            ).xpath(String.format("//*[@id='%s']/text()", param)).iterator();
            if (!items.hasNext()) {
                throw new IllegalArgumentException(
                    String.format(
                        "Policy item '%s' not found", param
                    )
                );
            }
            result = items.next();
        }
        return result;
    }

}
