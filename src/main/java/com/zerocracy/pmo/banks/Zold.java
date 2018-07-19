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

import com.jcabi.http.request.JdkRequest;
import com.jcabi.http.response.RestResponse;
import com.zerocracy.Farm;
import com.zerocracy.cash.Cash;
import com.zerocracy.farm.props.Props;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Zold payment.
 * @since 1.0
 */
public final class Zold implements Bank {

    /**
     * Props.
     */
    private final Props props;

    /**
     * Ctor.
     *
     * @param farm Farm
     */
    public Zold(final Farm farm) {
        this(new Props(farm));
    }

    /**
     * Ctor.
     *
     * @param props Props
     */
    public Zold(final Props props) {
        this.props = props;
    }

    @Override
    public Cash fee(final Cash amount) {
        return Cash.ZERO;
    }

    @Override
    public String pay(final String target, final Cash amount,
        final String details) throws IOException {
        final int status = new JdkRequest(this.props.get("//zold/host"))
            .uri()
            .path("/do-pay")
            .back()
            .method("POST")
            .header("X-Zold-Wts", this.props.get("//zold/secret"))
            .body()
            .formParam("bnf", Zold.enc(target))
            .formParam("amount", Zold.enc(amount.decimal().toString()))
            .formParam("details", Zold.enc(details))
            .back()
            .fetch()
            .as(RestResponse.class)
            .status();
        if (status != HttpURLConnection.HTTP_SEE_OTHER) {
            throw new IOException(
                String.format("Zold payment failed, code=%d", status)
            );
        }
        return "";
    }

    @Override
    public void close() throws IOException {
        // Nothing to do
    }

    /**
     * URL-encode string.
     *
     * @param src Source to encode
     * @return Encoded value
     * @throws UnsupportedEncodingException If fails
     */
    private static String enc(final String src)
        throws UnsupportedEncodingException {
        return URLEncoder.encode(src, StandardCharsets.UTF_8.name());
    }
}
