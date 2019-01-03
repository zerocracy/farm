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

import java.math.BigDecimal;
import java.time.Instant;
import javax.json.JsonObject;

/**
 * Coinbase transaction.
 *
 * @since 1.0
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class CbTransaction {

    /**
     * Json.
     */
    private final JsonObject jsn;

    /**
     * Ctor.
     * @param jsn Json
     */
    public CbTransaction(final JsonObject jsn) {
        this.jsn = jsn;
    }

    /**
     * Id.
     * @return Id
     */
    public String tid() {
        return this.jsn.getString("id");
    }

    /**
     * Status.
     * @return Status
     */
    public String status() {
        return this.jsn.getString("status");
    }

    /**
     * Timestamp.
     * @return Created
     */
    public Instant created() {
        return Instant.parse(this.jsn.getString("created_at"));
    }

    /**
     * Amount.
     * @return Amount
     */
    public BigDecimal amount() {
        return new BigDecimal(
            this.jsn.getJsonObject("amount")
                .getString("amount")
        );
    }
}
