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
package com.zerocracy.pmo.banks;

import com.jcabi.http.request.JdkRequest;
import com.jcabi.http.response.RestResponse;
import com.zerocracy.Farm;
import com.zerocracy.farm.props.Props;
import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import javax.json.Json;

/**
 * Zold rate to USD.
 *
 * @since 1.0
 */
public final class ZldRates {

    /**
     * Farm.
     */
    private final Farm farm;

    /**
     * Ctor.
     * @param farm Farm
     */
    public ZldRates(final Farm farm) {
        this.farm = farm;
    }

    /**
     * USD rate.
     * @return Rate of ZLD:USD
     * @throws IOException If fails
     */
    public BigDecimal usd() throws IOException {
        final Props props = new Props(this.farm);
        final RestResponse rsp = new JdkRequest(props.get("//zold/host"))
            .uri()
            .path("/rate.json")
            .queryParam("noredirect", "1")
            .back()
            .method("GET")
            .header("X-Zold-Wts", props.get("//zold/secret"))
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
}
