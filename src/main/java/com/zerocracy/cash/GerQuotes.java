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
package com.zerocracy.cash;

import com.jcabi.http.request.JdkRequest;
import com.jcabi.http.response.JsonResponse;
import com.jcabi.http.response.RestResponse;
import java.io.IOException;
import java.net.HttpURLConnection;
import javax.json.JsonObject;

/**
 * Quotes.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.6
 */
public final class GerQuotes implements Quotes {

    @Override
    public double quote(final Currency src, final Currency dest) {
        if (!src.equals(Currency.USD) && !dest.equals(Currency.USD)) {
            throw new IllegalArgumentException(
                String.format(
                    "At least one currency must be USD, %s/%s",
                    src, dest
                )
            );
        }
        final double quote;
        if (src.equals(Currency.USD)) {
            final JsonObject json;
            try {
                json = new JdkRequest("http://apilayer.net/api/live")
                    .uri()
                    .queryParam(
                        "access_key",
                        "b747f2bc60d143d14de9d418fd009d9a"
                    )
                    .back()
                    .header("Accept", "application/json")
                    .fetch()
                    .as(RestResponse.class)
                    .assertStatus(HttpURLConnection.HTTP_OK)
                    .as(JsonResponse.class)
                    .json().readObject();
            } catch (final IOException ex) {
                throw new IllegalStateException(ex);
            }
            quote = json.getJsonObject("quotes").getJsonNumber(
                String.format("%s%s", src, dest)
            ).bigDecimalValue().doubleValue();
        } else {
            quote = 1.0d / this.quote(dest, src);
        }
        return quote;
    }

}
