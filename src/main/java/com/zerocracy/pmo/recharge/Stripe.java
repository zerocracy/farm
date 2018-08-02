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
package com.zerocracy.pmo.recharge;

import com.stripe.exception.APIConnectionException;
import com.stripe.exception.APIException;
import com.stripe.exception.AuthenticationException;
import com.stripe.exception.CardException;
import com.stripe.exception.InvalidRequestException;
import com.stripe.model.Charge;
import com.stripe.model.Customer;
import com.stripe.net.RequestOptions;
import com.zerocracy.Farm;
import com.zerocracy.Par;
import com.zerocracy.cash.Cash;
import com.zerocracy.claims.ClaimOut;
import com.zerocracy.entry.ClaimsOf;
import com.zerocracy.farm.props.Props;
import java.io.IOException;
import org.cactoos.map.MapEntry;
import org.cactoos.map.SolidMap;

/**
 * Stripe payment.
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class Stripe {

    /**
     * Farm.
     */
    private final Farm farm;

    /**
     * Ctor.
     * @param frm Farm
     */
    public Stripe(final Farm frm) {
        this.farm = frm;
    }

    /**
     * Register a customer.
     * @param token Token from HTML front
     * @param email Email of the customer
     * @param amount How much to charge
     * @param details The payment details
     * @return Payment ID from Stripe
     * @throws IOException If fails
     * @checkstyle ParameterNumberCheck (5 lines)
     */
    public String pay(final String token, final String email,
        final Cash amount, final String details)
        throws IOException {
        return this.charge(this.register(token, email), amount, details);
    }

    /**
     * Register a customer.
     * @param token Token from HTML front
     * @param email Email of the customer
     * @return Customer ID from Stripe
     * @throws IOException If fails
     */
    public String register(final String token, final String email)
        throws IOException {
        final String cid;
        try {
            cid = Customer.create(
                new SolidMap<String, Object>(
                    new MapEntry<>("email", email),
                    new MapEntry<>("source", token)
                ),
                this.options()
            ).getId();
        } catch (final APIException | APIConnectionException | CardException
            | AuthenticationException | InvalidRequestException
            | IOException ex) {
            throw new Stripe.PaymentException(ex);
        }
        new ClaimOut().type("Notify PMO").param(
            "message", new Par(
                "Stripe customer `%s` registered as %s"
            ).say(cid, email)
        ).postTo(new ClaimsOf(this.farm));
        return cid;
    }

    /**
     * Make a payment.
     * @param customer Customer ID
     * @param amount How much to charge
     * @param details The payment details
     * @return Customer ID from Stripe
     * @throws IOException If fails
     */
    public String charge(final String customer, final Cash amount,
        final String details) throws IOException {
        // @checkstyle MagicNumber (1 line)
        final int cents = (int) (amount.decimal().doubleValue() * 100.0d);
        final String pid;
        try {
            pid = Charge.create(
                new SolidMap<String, Object>(
                    new MapEntry<>("customer", customer),
                    new MapEntry<>("amount", cents),
                    new MapEntry<>("currency", "usd"),
                    new MapEntry<>(
                        "description",
                        new Par.ToText(details).toString()
                    )
                ),
                this.options()
            ).getId();
        } catch (final APIException | APIConnectionException | CardException
            | AuthenticationException | InvalidRequestException
            | IOException ex) {
            throw new Stripe.PaymentException(ex);
        }
        new ClaimOut().type("Notify PMO").param(
            "message", new Par(
                "Stripe customer `%s` charged %s for \"%s\": `%s`"
            ).say(customer, amount, details, pid)
        ).postTo(new ClaimsOf(this.farm));
        return pid;
    }

    /**
     * Request options.
     * @return Options
     * @throws IOException If fails
     */
    private RequestOptions options() throws IOException {
        return new RequestOptions.RequestOptionsBuilder().setApiKey(
            new Props(this.farm).get("//stripe/secret", "")
        ).build();
    }

    /**
     * Failure.
     */
    public static final class PaymentException extends IOException {
        /**
         * Serialization marker.
         */
        private static final long serialVersionUID = 9204303106888716333L;
        /**
         * Ctor.
         * @param cause The cause
         */
        public PaymentException(final Throwable cause) {
            super(cause);
        }
    }

}
