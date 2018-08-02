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
package com.zerocracy.pmo.banks;

import com.jcabi.aspects.Tv;
import com.jcabi.log.Logger;
import com.paypal.exception.ClientActionRequiredException;
import com.paypal.exception.HttpErrorException;
import com.paypal.exception.InvalidCredentialException;
import com.paypal.exception.InvalidResponseDataException;
import com.paypal.exception.MissingCredentialException;
import com.paypal.exception.SSLConfigurationException;
import com.paypal.sdk.exceptions.OAuthException;
import com.paypal.svcs.services.AdaptivePaymentsService;
import com.paypal.svcs.types.ap.PayRequest;
import com.paypal.svcs.types.ap.PayResponse;
import com.paypal.svcs.types.ap.Receiver;
import com.paypal.svcs.types.ap.ReceiverList;
import com.paypal.svcs.types.common.AckCode;
import com.paypal.svcs.types.common.ErrorData;
import com.paypal.svcs.types.common.RequestEnvelope;
import com.zerocracy.Farm;
import com.zerocracy.Par;
import com.zerocracy.cash.Cash;
import com.zerocracy.cash.CashParsingException;
import com.zerocracy.claims.ClaimOut;
import com.zerocracy.claims.ClaimOutSafe;
import com.zerocracy.entry.ClaimsOf;
import com.zerocracy.farm.props.Props;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import org.cactoos.map.MapEntry;
import org.cactoos.map.SolidMap;

/**
 * Paypal payment method.
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@SuppressWarnings("PMD.ExcessiveImports")
final class Paypal implements Bank {

    /**
     * Farm.
     */
    private final Farm farm;

    /**
     * Ctor.
     * @param frm The farm
     */
    Paypal(final Farm frm) {
        this.farm = frm;
    }

    @Override
    public Cash fee(final Cash amount) throws CashParsingException {
        return amount.add(new Cash.S("$0.30"))
            // @checkstyle MagicNumber (1 line)
            .mul((long) Tv.THOUSAND).div(926L).add(amount.mul(-1L));
    }

    @Override
    public String pay(final String target, final Cash amount,
        final String details) throws IOException {
        if (details.length() >= Tv.THOUSAND) {
            throw new IllegalArgumentException(
                String.format(
                    "PayPal details too long, the API won't accept it: %s",
                    details
                )
            );
        }
        final Props props = new Props(this.farm);
        final AdaptivePaymentsService service = new AdaptivePaymentsService(
            new SolidMap<String, String>(
                new MapEntry<>("mode", props.get("//paypal/mode")),
                new MapEntry<>(
                    "acct1.UserName",
                    props.get("//paypal/username")
                ),
                new MapEntry<>(
                    "acct1.Password",
                    props.get("//paypal/password")
                ),
                new MapEntry<>(
                    "acct1.Signature",
                    props.get("//paypal/signature")
                ),
                new MapEntry<>(
                    "acct1.AppId",
                    props.get("//paypal/appid")
                )
            )
        );
        final PayResponse response;
        final PayRequest request;
        try {
            request = this.request(
                target,
                amount.decimal().doubleValue(),
                details
            );
            response = Paypal.valid(service.pay(request));
        } catch (final SSLConfigurationException
            | InvalidCredentialException
            | InvalidResponseDataException
            | InterruptedException
            | ClientActionRequiredException
            | MissingCredentialException
            | OAuthException
            | HttpErrorException | IOException ex) {
            throw new IOException(
                String.format(
                    "Failed to pay %s to %s with memo \"%s\": %s %s",
                    amount, target, details,
                    ex.getClass().getName(), ex.getMessage()
                ),
                ex
            );
        }
        new ClaimOutSafe(
            new ClaimOut().type("Notify PMO").param(
                "message",
                new Par(
                    "PayPal payment has been sent;",
                    "Sender=%s, Receiver=%s,",
                    "Memo=\"%s\",",
                    "Amount=%s, Currency=%s,",
                    "TrackingId=%s,",
                    "Ack=%s, PayKey=%s, PaymentExecStatus=%s,",
                    "Build=%s, CorrelationId=%s,",
                    "Timestamp=%s"
                ).say(
                    request.getSenderEmail(),
                    request.getReceiverList().getReceiver().get(0).getEmail(),
                    request.getMemo(),
                    request.getReceiverList().getReceiver().get(0).getAmount(),
                    request.getCurrencyCode(),
                    request.getTrackingId(),
                    response.getResponseEnvelope().getAck().getValue(),
                    response.getPayKey(),
                    response.getPaymentExecStatus(),
                    response.getResponseEnvelope().getBuild(),
                    response.getResponseEnvelope().getCorrelationId(),
                    response.getResponseEnvelope().getTimestamp()
                )
            )
        ).postTo(new ClaimsOf(this.farm));
        return response.getPayKey();
    }

    @Override
    public void close() throws IOException {
        // Nothing to do
    }

    /**
     * Make a request.
     * @param email Email
     * @param amount Amount to charge, in USD
     * @param memo Memo
     * @return Request
     * @throws IOException If fails
     * @link https://developer.paypal.com/docs/classic/api/adaptive-payments/Pay_API_Operation/
     */
    private PayRequest request(final String email,
        final double amount, final String memo)
        throws IOException {
        final RequestEnvelope env = new RequestEnvelope();
        env.setErrorLanguage("en_US");
        final Receiver receiver = new Receiver();
        receiver.setAmount(amount);
        receiver.setEmail(email);
        final PayRequest request = new PayRequest();
        request.setReceiverList(
            new ReceiverList(Collections.singletonList(receiver))
        );
        request.setSenderEmail(new Props(this.farm).get("//paypal/email"));
        request.setRequestEnvelope(env);
        request.setCurrencyCode("USD");
        request.setFeesPayer("SENDER");
        request.setCancelUrl("http://www.zerocracy.com/terms.html#cancel");
        request.setReturnUrl("http://www.zerocracy.com/terms.html#return");
        request.setActionType("PAY");
        request.setMemo(memo);
        Logger.info(Paypal.class, request.toNVPString());
        return request;
    }

    /**
     * Validate and return a response.
     * @param response Response
     * @return Response
     * @throws IOException If fails
     */
    private static PayResponse valid(final PayResponse response)
        throws IOException {
        final Collection<ErrorData> errors = response.getError();
        if (!errors.isEmpty()) {
            final Collection<String> msgs = new LinkedList<>();
            for (final ErrorData error : errors) {
                Logger.error(
                    Paypal.class,
                    "%d %s %s %s", error.getErrorId(),
                    error.getCategory(), error.getDomain(),
                    error.getMessage()
                );
                msgs.add(
                    String.format(
                        "%s %s %s", error.getSeverity(),
                        error.getCategory(), error.getMessage()
                    )
                );
            }
            throw new IOException(
                String.format("Failed to pay through PayPal: %s", msgs)
            );
        }
        if (response.getResponseEnvelope().getAck() != AckCode.SUCCESS) {
            throw new IOException(
                String.format(
                    "PayPal ACK code is not SUCCESS: %s",
                    response.getResponseEnvelope().getAck()
                )
            );
        }
        if (!"COMPLETED".equals(response.getPaymentExecStatus())) {
            throw new IOException(
                String.format(
                    "PayPal exec status is not COMPLETED: %s",
                    response.getPaymentExecStatus()
                )
            );
        }
        return response;
    }
}
