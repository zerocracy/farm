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
package com.zerocracy.pmo.banks;

import com.jcabi.xml.XMLDocument;
import com.zerocracy.cash.Cash;
import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test case for {@link FkBank}.
 * @author Izbassar Tolegen (t.izbassar@gmail.com)
 * @version $Id$
 * @since 0.22
 * @checkstyle JavadocMethodCheck (500 lines)
 */
public final class FkBankTest {

    @Test
    @Ignore
    public void equalsWorks() throws IOException {
        final Path dir = Files.createTempDirectory("eq");
        final String name = "test.xml";
        final Path path = dir.resolve(name);
        try (final Closeable bank = new FkBank(path)) {
            MatcherAssert.assertThat(
                bank,
                Matchers.equalTo(new FkBank(path))
            );
        }
    }

    @Test
    @Ignore
    public void savesCalculatedFee() throws IOException {
        final Path file = FkBankTest.temp("x0");
        try (final Bank bank = new FkBank(file)) {
            bank.fee(new Cash.S("$0.50"));
            MatcherAssert.assertThat(
                new XMLDocument(file).xpath("/fees/fee/result"),
                Matchers.contains(
                    "$0.80"
                )
            );
        }
    }

    @Test
    @Ignore
    public void savesRequestedAmountForFeeCalculation() throws IOException {
        final Path file = FkBankTest.temp("x1");
        try (final Bank bank = new FkBank(file)) {
            final String amount = "$0.75";
            bank.fee(new Cash.S(amount));
            MatcherAssert.assertThat(
                new XMLDocument(file).xpath("/fees/fee/amount"),
                Matchers.contains(amount)
            );
        }
    }

    @Test
    @Ignore
    public void savesPayId() throws IOException {
        final Path file = FkBankTest.temp("x2");
        try (final Bank bank = new FkBank(file)) {
            final String id = bank.pay(
                "trgt2", new Cash.S("$0.60"), "dtls2"
            );
            MatcherAssert.assertThat(
                new XMLDocument(file).xpath("/payments/payment/result"),
                Matchers.contains(id)
            );
        }
    }

    @Test
    @Ignore
    public void savesPayAmount() throws IOException {
        final Path file = FkBankTest.temp("x3");
        try (final Bank bank = new FkBank(file)) {
            final String amount = "$1.60";
            bank.pay(
                "trgt3", new Cash.S(amount), "dtls3"
            );
            MatcherAssert.assertThat(
                new XMLDocument(file).xpath("/payments/payment/amount"),
                Matchers.contains(amount)
            );
        }
    }

    @Test
    @Ignore
    public void savesPayTarget() throws IOException {
        final Path file = FkBankTest.temp("x4");
        try (final Bank bank = new FkBank(file)) {
            final String target = "some_target";
            bank.pay(
                target, new Cash.S("$1.65"), "dtls4"
            );
            MatcherAssert.assertThat(
                new XMLDocument(file).xpath("/payments/payment/target"),
                Matchers.contains(target)
            );
        }
    }

    @Test
    @Ignore
    public void savesPayDetails() throws IOException {
        final Path file = FkBankTest.temp("x5");
        try (final Bank bank = new FkBank(file)) {
            final String details = "some_details";
            bank.pay(
                "trgt5", new Cash.S("$1.68"), details
            );
            MatcherAssert.assertThat(
                new XMLDocument(file).xpath("/payments/payment/details"),
                Matchers.contains(details)
            );
        }
    }

    private static Path temp(final String prefix) throws IOException {
        return Files.createTempFile(prefix, ".xml");
    }
}
