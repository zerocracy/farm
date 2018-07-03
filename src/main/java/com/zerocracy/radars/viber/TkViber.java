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
package com.zerocracy.radars.viber;

import com.zerocracy.Farm;
import com.zerocracy.Par;
import com.zerocracy.tk.RsParFlash;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.logging.Level;
import javax.json.Json;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonReader;
import org.takes.Request;
import org.takes.Response;
import org.takes.Take;
import org.takes.facets.forward.RsForward;
import org.takes.rs.RsWithStatus;

/**
 * Viber webhook entry point.
 *
 * @author Carlos Miranda (miranda.cma@gmail.com)
 * @version $Id$
 * @since 0.22
 * @checkstyle ClassDataAbstractionCouplingCheck (2 lines)
 */
public final class TkViber implements Take {

    /**
     * Farm to use.
     */
    private final Farm farm;

    /**
     * Reactions.
     */
    private final Reaction reaction;

    /**
     * Constructor.
     * @param farm Farm to use
     */
    public TkViber(final Farm farm) {
        this(farm, new ReProfile());
    }

    /**
     * Constructor.
     * @param farm Farm to use
     * @param react Reaction to use
     */
    TkViber(final Farm farm, final Reaction react) {
        this.farm = farm;
        this.reaction = react;
    }

    @Override
    public Response act(final Request req) throws IOException {
        final JsonObject json = TkViber.json(
            new InputStreamReader(req.body(), StandardCharsets.UTF_8)
        );
        if (json.isEmpty()) {
            throw new RsForward(
                new RsParFlash(
                    new Par(
                        "We expect this URL to be called by Viber",
                        "with JSON as body of the request"
                    ).say(),
                    Level.WARNING
                )
            );
        }
        if (Objects.equals(json.getString("event"), "message")) {
            this.reaction.react(
                new VbBot(), this.farm, new VbEvent.Simple(json)
            );
        }
        return new RsWithStatus(
            HttpURLConnection.HTTP_OK
        );
    }

    /**
     * Read JSON from body.
     * @param body The body
     * @return The JSON object
     */
    private static JsonObject json(final Reader body) {
        try (
            final JsonReader reader = Json.createReader(body)
        ) {
            return reader.readObject();
        } catch (final JsonException ex) {
            throw new IllegalArgumentException(
                String.format("Can't parse JSON: %s", body),
                ex
            );
        }
    }
}
