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
package com.zerocracy.zold;

import com.jcabi.aspects.Tv;
import com.jcabi.http.Request;
import com.jcabi.http.request.JdkRequest;
import com.jcabi.http.response.JsonResponse;
import com.jcabi.http.response.RestResponse;
import com.jcabi.http.wire.VerboseWire;
import com.zerocracy.Farm;
import com.zerocracy.cash.Cash;
import com.zerocracy.cash.Currency;
import com.zerocracy.farm.props.Props;
import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.text.DecimalFormat;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.json.Json;
import javax.json.JsonObject;

/**
 * WTS-zold API.
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 * @checkstyle ParameterNumberCheck (500 lines)
 */
@SuppressWarnings({"PMD.UseObjectForClearerAPI", "PMD.AvoidDuplicateLiterals"})
public final class Zold {

    /**
     * Amount format.
     */
    private static final DecimalFormat FMT = new DecimalFormat();

    static {
        Zold.FMT.setMaximumFractionDigits(2);
        Zold.FMT.setMinimumFractionDigits(2);
        Zold.FMT.setGroupingUsed(false);
    }

    /**
     * Farm.
     */
    private final Farm farm;

    /**
     * Ctor.
     * @param farm Farm
     */
    public Zold(final Farm farm) {
        this.farm = farm;
    }

    /**
     * Pay with Zold.
     *
     * @param target Recepient wallet
     * @param amount Amount of ZLD to pay
     * @param details Transaction details
     * @return WTS job id
     * @throws IOException If fails
     * @checkstyle ParameterNumberCheck (5 lines)
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public String pay(final String target, final BigDecimal amount,
        final String details) throws IOException {
        final Props props = new Props(this.farm);
        final RestResponse rsp = this.wtsRequest(Request.POST, "/do-pay")
            .body()
            .formParam("bnf", String.format("@%s", target))
            .formParam("amount", Zold.FMT.format(amount))
            .formParam("details", new ZoldDetails(details).asString())
            .formParam("keygap", props.get("//zold/keygap"))
            .back()
            .header("Content-Type", "application/x-www-form-urlencoded")
            .through(VerboseWire.class)
            .fetch()
            .as(RestResponse.class);
        if (rsp.status() != HttpURLConnection.HTTP_OK) {
            new ZldError(rsp).raise("Zold payment failed");
        }
        final List<String> hds = rsp.headers().get("X-Zold-Job");
        if (hds.isEmpty()) {
            throw new IOException(
                "Zold response doesn't have job-id"
            );
        }
        final String job = hds.get(0);
        String status;
        do {
            try {
                TimeUnit.SECONDS.sleep((long) Tv.FIVE);
            } catch (final InterruptedException err) {
                Thread.currentThread().interrupt();
                throw new IOException("Thread interrupted", err);
            }
            final RestResponse jrsp = this.wtsRequest(Request.GET, "/job")
                .uri()
                .queryParam("id", job)
                .back()
                .fetch()
                .as(RestResponse.class);
            status = jrsp.body();
            if (jrsp.status() != HttpURLConnection.HTTP_OK) {
                new ZldError(jrsp).raise(
                    String.format(
                        "WTS job failed, job-id=%s, status=%s", job, status
                    )
                );
            }
        } while ("RUNNING".equalsIgnoreCase(status));
        if (!"OK".equals(status)) {
            throw new IOException(
                String.format(
                    "Failed to pay via WTS: job=%s error=%s",
                    job, status
                )
            );
        }
        return job;
    }

    /**
     * USD rate.
     * @return Rate of ZLD:USD
     * @throws IOException If fails
     */
    public BigDecimal rate() throws IOException {
        final RestResponse rsp = this.wtsRequest(Request.GET, "/rate.json")
            .fetch()
            .as(RestResponse.class);
        if (rsp.status() != HttpURLConnection.HTTP_OK) {
            new ZldError(rsp).raise("Failed to get rates");
        }
        return Json.createReader(new StringReader(rsp.body()))
            .readObject()
            .getJsonNumber("usd_rate")
            .bigDecimalValue();
    }

    /**
     * Create new invoice.
     * @return Invoice
     * @throws IOException If fails
     */
    public ZldInvoice invoice() throws IOException {
        final RestResponse rsp = this.wtsRequest(Request.GET, "/invoice.json")
            .fetch()
            .as(RestResponse.class);
        if (rsp.status() != HttpURLConnection.HTTP_OK) {
            new ZldError(rsp).raise("Failed to get invoice");
        }
        return new Zold.JsonInvoice(
            rsp.as(JsonResponse.class).json().readObject()
        );
    }

    /**
     * Register callback.
     * @param wallet Wallet
     * @param pref Prefix
     * @param ptn Regex pattern
     * @param callback Callback URI
     * @param secret Secret token
     * @return Callback id
     * @throws IOException If fails
     */
    public String subscribe(final String wallet, final String pref,
        final String ptn, final String callback, final String secret)
        throws IOException {
        final RestResponse rsp = this.wtsRequest(Request.GET, "/wait-for")
            .uri()
            .queryParam("wallet", wallet)
            .queryParam("prefix", pref)
            .queryParam("regexp", ptn)
            .queryParam("uri", callback)
            .queryParam("token", secret)
            .back()
            .fetch()
            .as(RestResponse.class);
        if (rsp.status() != HttpURLConnection.HTTP_OK) {
            new ZldError(rsp).raise("Failed to register callback");
        }
        return rsp.body();
    }

    /**
     * Current wallet id.
     * @return Id stirng
     * @throws IOException If fails
     */
    public String wallet() throws IOException {
        final RestResponse rsp = this.wtsRequest(Request.GET, "/id")
            .fetch()
            .as(RestResponse.class);
        if (rsp.status() != HttpURLConnection.HTTP_OK) {
            new ZldError(rsp).raise("Failed to find wallet id");
        }
        return rsp.body();
    }

    /**
     * Pull the wallet.
     * @throws IOException If fails
     */
    public void pull() throws IOException {
        final RestResponse rsp = this.wtsRequest(Request.GET, "/pull")
            .fetch()
            .as(RestResponse.class);
        if (rsp.status() != HttpURLConnection.HTTP_OK) {
            new ZldError(rsp).raise("Failed to find wallet id");
        }
    }

    /**
     * Notify WTS on funded.
     * @param cash Amount of cash
     * @throws IOException If fails
     */
    public void funded(final Cash cash) throws IOException {
        final RestResponse rsp = this.wtsRequest(Request.GET, "/funded")
            .uri()
            .queryParam(
                "amount",
                Zold.FMT.format(cash.exchange(Currency.USD).decimal())
            ).back()
            .fetch()
            .as(RestResponse.class);
        if (rsp.status() != HttpURLConnection.HTTP_OK) {
            new ZldError(rsp).raise("Failed to send funded callback");
        }
    }

    /**
     * Base WTS request.
     * @param method HTTP method
     * @param path URL path
     * @return HTTP request
     * @throws IOException If fails
     */
    private Request wtsRequest(final String method, final String path)
        throws IOException {
        final Props props = new Props(this.farm);
        return new JdkRequest(props.get("//zold/host"))
            .method(method)
            .uri()
            .path(path)
            .queryParam("noredirect", "1")
            .back()
            .header("X-Zold-Wts", props.get("//zold/secret"));
    }

    /**
     * Json invoice.
     */
    private static final class JsonInvoice implements ZldInvoice {

        /**
         * Json.
         */
        private final JsonObject jsn;

        /**
         * Ctor.
         * @param jsn Json
         */
        private JsonInvoice(final JsonObject jsn) {
            this.jsn = jsn;
        }

        @Override
        public String prefix() throws IOException {
            return this.jsn.getString("prefix");
        }

        @Override
        public String invoice() throws IOException {
            return this.jsn.getString("invoice");
        }
    }
}
