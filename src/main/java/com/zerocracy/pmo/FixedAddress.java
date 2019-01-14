/*
 * Copyright (c) 2016-2019 Zerocracy
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

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.cactoos.Text;

/**
 * Fixed wallet address.
 *
 * @since 1.0
 */
final class FixedAddress implements Text {

    /**
     * Pattern to handle Slack email autoformatting issue, see
     * https://github.com/zerocracy/farm/issues/1848 bug for details.
     * @checkstyle LineLengthCheck (5 line)
     */
    private static final Pattern PAYPAL_LINK_EMAIL =
        Pattern.compile("^<mailto:(?:[\\w.%-]+@[-.\\w]+\\.[A-Za-z]{2,4})\\|(?<email>[\\w.%-]+@[-.\\w]+\\.[A-Za-z]{2,4})>$");

    /**
     * Bank.
     */
    private final String bank;

    /**
     * Source.
     */
    private final String address;

    /**
     * Ctor.
     * @param bank Bank
     * @param address Address
     */
    FixedAddress(final String bank, final String address) {
        this.bank = bank;
        this.address = address;
    }

    @Override
    public String asString() {
        final String res;
        final Matcher pplink =
            FixedAddress.PAYPAL_LINK_EMAIL.matcher(this.address);
        if (Objects.equals("paypal", this.bank)
            && pplink.matches()) {
            res = pplink.group("email");
        } else {
            res = this.address;
        }
        return res;
    }
}
