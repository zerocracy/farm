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
package com.zerocracy.pmo.banks.coinbase;

import com.jcabi.http.Request;
import com.jcabi.http.Response;
import com.jcabi.http.Wire;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import javax.crypto.spec.SecretKeySpec;
import org.cactoos.Bytes;
import org.cactoos.crypto.mac.MacFrom;
import org.cactoos.crypto.mac.MacOf;
import org.cactoos.crypto.mac.MacSpec;
import org.cactoos.io.InputOf;
import org.cactoos.list.ListOf;
import org.cactoos.map.MapEntry;
import org.cactoos.text.HexOf;
import org.cactoos.text.JoinedText;

/**
 * Wire to sign requests.
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class SignWire implements Wire {

    /**
     * Hmac name.
     */
    private static final String HMAC = "HmacSHA256";

    /**
     * Origin wire.
     */
    private final Wire origin;

    /**
     * API key.
     */
    private final String key;

    /**
     * API secret.
     */
    private final String secret;

    /**
     * Ctor.
     * @param origin Origin
     * @param key Key
     * @param secret SEcret
     */
    public SignWire(final Wire origin, final String key,
        final String secret) {
        this.origin = origin;
        this.key = key;
        this.secret = secret;
    }

    // @checkstyle ParameterNumberCheck (7 lines)
    @Override
    public Response send(final Request req, final String home,
        final String method,
        final Collection<Map.Entry<String, String>> headers,
        final InputStream content, final int connect,
        final int read) throws IOException {
        final Collection<Map.Entry<String, String>> hds =
            new ArrayList<>(new ListOf<>(headers));
        final long timestamp = Instant.now().getEpochSecond();
        hds.add(
            new MapEntry<>("CB-ACCESS-KEY", this.key)
        );
        hds.add(
            new MapEntry<>(
                "CB-ACCESS-TIMESTAMP",
                Long.toString(timestamp)
            )
        );
        final String query;
        if (req.uri().get().getQuery() == null) {
            query = "";
        } else {
            query = String.format("?%s", req.uri().get().getQuery());
        }
        final Bytes hmac = new MacOf(
            new InputOf(
                new JoinedText(
                    "",
                    Long.toString(timestamp),
                    method.toUpperCase(Locale.US),
                    req.uri().get().getPath(),
                    query,
                    req.body().get()
                )
            ),
            new MacFrom(
                SignWire.HMAC,
                new MacSpec(
                    new SecretKeySpec(
                        this.secret.getBytes(StandardCharsets.US_ASCII),
                        SignWire.HMAC
                    )
                )
            )
        );
        hds.add(
            new MapEntry<>(
                "CB-ACCESS-SIGN",
                new HexOf(hmac).asString()
            )
        );
        return this.origin.send(
            req, home, method, hds, content, connect, read
        );
    }
}
