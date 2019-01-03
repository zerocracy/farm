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
package com.zerocracy.pmo.banks.coinbase;

import com.jcabi.aspects.Tv;
import com.jcabi.http.request.JdkRequest;
import com.jcabi.http.response.JsonResponse;
import com.jcabi.http.response.RestResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.time.Instant;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import org.cactoos.collection.Mapped;

/**
 * Coinbase account API.
 *
 * @since 1.0
 */
@SuppressWarnings(
    {"PMD.AvoidInstantiatingObjectsInLoops", "PMD.AvoidDuplicateLiterals"}
)
public final class Coinbase {

    /**
     * Coinbase host.
     */
    private static final String HOST = "https://api.coinbase.com";

    /**
     * API key.
     */
    private final String key;

    /**
     * API secret.
     */
    private final String secret;

    /**
     * Account id.
     */
    private final String acc;

    /**
     * Ctor.
     * @param key Key
     * @param secret Secret
     * @param account Account
     */
    public Coinbase(final String key, final String secret,
        final String account) {
        this.key = key;
        this.secret = secret;
        this.acc = account;
    }

    /**
     * Account scopes.
     *
     * @return Scopes set
     * @throws IOException If fails
     */
    public Set<String> scopes() throws IOException {
        final JsonArray jscopes = new JdkRequest(Coinbase.HOST)
            .method("GET")
            .uri()
            .path("/v2/user/auth")
            .back()
            .through(SignWire.class, this.key, this.secret)
            .fetch()
            .as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_OK)
            .as(JsonResponse.class)
            .json()
            .readObject()
            .getJsonObject("data")
            .getJsonArray("scopes");
        return new HashSet<>(
            new Mapped<>(
                jsn -> JsonString.class.cast(jsn).getString(),
                jscopes
            )
        );
    }

    /**
     * Balance.
     * @return Balance
     * @throws IOException If fails
     */
    public BigDecimal balance() throws IOException {
        final String amt = new JdkRequest(Coinbase.HOST)
            .method("GET")
            .uri()
            .path(String.format("/v2/accounts/%s", this.acc))
            .back()
            .through(SignWire.class, this.key, this.secret)
            .fetch()
            .as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_OK)
            .as(JsonResponse.class)
            .json()
            .readObject()
            .getJsonObject("data")
            .getJsonObject("balance")
            .getString("amount");
        return new BigDecimal(amt);
    }

    /**
     * Buy.
     * @param currency Currency to buy
     * @param amount Amount to buy
     * @param commit Commit operation
     * @return Bought
     * @throws IOException If fails
     */
    public Coinbase.Bought buy(final String currency, final BigDecimal amount,
        final boolean commit) throws IOException {
        final JsonObject data = new JdkRequest(Coinbase.HOST)
            .method("POST")
            .uri()
            .path(String.format("/v2/accounts/%s/buys", this.acc))
            .back()
            .body()
            .set(
                Json.createObjectBuilder()
                    .add("amount", amount.toString())
                    .add("currency", currency)
                    .build()
            ).back()
            .header("Content-Type", "application/json")
            .through(SignWire.class, this.key, this.secret)
            .fetch()
            .as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_CREATED)
            .as(JsonResponse.class)
            .json().readObject().getJsonObject("data");
        return new Coinbase.Bought(data);
    }

    /**
     * Transactions.
     *
     * @return Transactions
     * @throws IOException If fails
     * @todo #1815:30min We're loading all transactions here,
     *  it would be better to implement lazy loading for transaction pages
     *  and return iterable which will trigger next page loading
     *  when previous pages are read.
     */
    public Iterable<CbTransaction> transactions()
        throws IOException {
        String url = String.format(
            "%s/v2/accounts/%s/transactions",
            Coinbase.HOST, this.acc
        );
        final List<CbTransaction> txns = new LinkedList<>();
        int pages = Tv.FIVE;
        while (!url.isEmpty()) {
            final JsonObject jsn = new JdkRequest(url)
                .method("GET")
                .through(SignWire.class, this.key, this.secret)
                .fetch()
                .as(RestResponse.class)
                .assertStatus(HttpURLConnection.HTTP_OK)
                .as(JsonResponse.class)
                .json().readObject();
            url = String.format(
                "%s%s",
                Coinbase.HOST,
                jsn.getJsonObject("pagination")
                    .getString("next_uri", "")
            );
            for (final JsonValue item : jsn.getJsonArray("data")) {
                txns.add(new CbTransaction(JsonObject.class.cast(item)));
            }
            if (pages <= 0) {
                break;
            }
            --pages;
        }
        return txns;
    }

    /**
     * Send.
     *
     * @param currency Currency
     * @param addr Address
     * @param amount Amount
     * @param details Payment details
     * @param idem Unique string, see API docs
     * @return Transaction
     * @throws IOException If fails
     * @checkstyle ParameterNumberCheck (5 lines)
     */
    @SuppressWarnings("PMD.UseObjectForClearerAPI")
    public CbTransaction send(final String currency, final String addr,
        final BigDecimal amount, final String details,
        final String idem) throws IOException {
        final JsonObject data = new JdkRequest(Coinbase.HOST)
            .method("POST")
            .uri()
            .path(String.format("/v2/accounts/%s/transactions", this.acc))
            .back()
            .body()
            .set(
                Json.createObjectBuilder()
                    .add("type", "send")
                    .add("to", addr)
                    .add("amount", amount.toString())
                    .add("currency", currency)
                    .add("description", details)
                    .build()
            ).back()
            .header("Content-Type", "application/json")
            .through(SignWire.class, this.key, this.secret)
            .fetch()
            .as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_CREATED)
            .as(JsonResponse.class)
            .json().readObject().getJsonObject("data");
        return new CbTransaction(data);
    }

    /**
     * Bought.
     */
    public static final class Bought {

        /**
         * Json.
         */
        private final JsonObject jsn;

        /**
         * New bought.
         * @param jsn Json
         */
        public Bought(final JsonObject jsn) {
            this.jsn = jsn;
        }

        /**
         * Amount.
         * @return Decimal
         */
        public BigDecimal amount() {
            return new BigDecimal(
                this.jsn.getJsonObject("amount").getString("amount")
            );
        }

        /**
         * Total.
         * @return Decimal
         */
        public BigDecimal total() {
            return new BigDecimal(
                this.jsn.getJsonObject("total")
                    .getString("amount")
            );
        }

        /**
         * Timestamp.
         * @return Instant
         */
        public Instant timestamp() {
            return Instant.parse(
                this.jsn.getString("created_at")
            );
        }

        /**
         * Transaction id.
         * @return String id
         */
        public String tid() {
            return this.jsn.getJsonObject("transaction").getString("id");
        }

        /**
         * Bought status.
         * @return Status of bought
         */
        public String status() {
            return this.jsn.getString("status");
        }
    }
}
