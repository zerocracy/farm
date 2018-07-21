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
package com.zerocracy.radars.viber;

import com.zerocracy.Farm;
import com.zerocracy.farm.props.Props;
import java.io.IOException;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.util.Objects;
import javax.json.Json;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonReader;
import org.cactoos.text.TextOf;
import org.takes.HttpException;
import org.takes.Request;
import org.takes.Response;
import org.takes.Take;
import org.takes.rs.RsWithStatus;

/**
 * Viber webhook entry point.
 *
 * @since 1.0
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
     * Bot to use.
     */
    private final VbBot bot;

    /**
     * Constructor.
     * @param farm Farm to use
     * @throws IOException If an IO error occurs.
     */
    public TkViber(final Farm farm) throws IOException {
        this(
            farm,
            new VbBot(new Props(farm).get("//viber/token")),
            new ReProfile()
        );
    }

    /**
     * Constructor.
     * @param farm Farm to use
     * @param bot Viber bot
     */
    public TkViber(final Farm farm, final VbBot bot) {
        this(farm, bot, new ReProfile());
    }

    /**
     * Constructor.
     * @param farm Farm to use
     * @param bot Viber bot
     * @param react Reaction to use
     */
    TkViber(final Farm farm, final VbBot bot, final Reaction react) {
        this.farm = farm;
        this.reaction = react;
        this.bot = bot;
    }

    @Override
    public Response act(final Request req) throws IOException {
        final JsonObject callback = TkViber.json(
            new TextOf(req.body()).asString()
        );
        if (callback.isEmpty()) {
            throw new HttpException(HttpURLConnection.HTTP_BAD_REQUEST);
        }
        if (Objects.equals(callback.getString("event"), "message")) {
            this.reaction.react(
                this.bot, this.farm, new VbEvent.Simple(callback)
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
    private static JsonObject json(final String body) {
        try (
            final JsonReader reader = Json.createReader(new StringReader(body))
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
