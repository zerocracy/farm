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
package com.zerocracy.tk;

import com.stripe.exception.APIConnectionException;
import com.stripe.exception.APIException;
import com.stripe.exception.AuthenticationException;
import com.stripe.exception.CardException;
import com.stripe.exception.InvalidRequestException;
import com.stripe.model.Customer;
import com.stripe.net.RequestOptions;
import com.zerocracy.Farm;
import com.zerocracy.farm.props.Props;
import java.io.IOException;
import org.cactoos.map.MapEntry;
import org.cactoos.map.SolidMap;

/**
 * Stripe payment.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.19
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
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
     * Make a payment.
     * @param token Token from HTML front
     * @param email Email of the customer
     * @param details The payment details
     * @return Customer ID from Stripe
     * @throws Stripe.PaymentException If fails
     */
    public String pay(final String token, final String email,
        final String details) throws Stripe.PaymentException {
        try {
            return Customer.create(
                new SolidMap<String, Object>(
                    new MapEntry<>("email", email),
                    new MapEntry<>("source", token),
                    new MapEntry<>("description", details)
                ),
                new RequestOptions.RequestOptionsBuilder().setApiKey(
                    new Props(this.farm).get("//stripe/secret", "")
                ).build()
            ).getId();
        } catch (final APIException | APIConnectionException | CardException
            | AuthenticationException | InvalidRequestException
            | IOException ex) {
            throw new Stripe.PaymentException(ex);
        }
    }

    /**
     * Failure.
     */
    public static final class PaymentException extends Exception {
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
