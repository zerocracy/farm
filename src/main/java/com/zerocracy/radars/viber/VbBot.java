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

import com.jcabi.http.Request;
import com.jcabi.http.request.JdkRequest;
import com.zerocracy.Farm;
import com.zerocracy.farm.props.Props;
import java.io.IOException;
import javax.json.Json;

/**
 * Viber bot.
 *
 * @since 1.0
 */
public final class VbBot {

    /**
     * Authentication token.
     */
    private final String token;

    /**
     * Resource URL.
     */
    private final String endpoint;

    /**
     * Ctor.
     * @param farm Farm
     * @throws IOException If an IO Exception occurs.
     */
    public VbBot(final Farm farm) throws IOException {
        this(new Props(farm).get("//viber/token"));
    }

    /**
     * Ctor.
     * @param token Authentication token
     */
    VbBot(final String token) {
        this(token, "https://chatapi.viber.com/pa/send_message");
    }

    /**
     * Ctor.
     * @param token Authentication token
     * @param uri Resource URI
     */
    VbBot(final String token, final String uri) {
        this.token = token;
        this.endpoint = uri;
    }

    /**
     * Send a message to a user.
     * @param id User
     * @param text Message text
     * @throws IOException If an IO exception occurs
     */
    @SuppressWarnings("PMD.AvoidDuplicateLiterals")
    public void sendMessage(final String id, final String text)
        throws IOException {
        new JdkRequest(this.endpoint)
            .method(Request.POST)
            .header("X-Viber-Auth-Token", this.token)
            .body()
            .set(
                Json.createObjectBuilder()
                    .add("receiver", id)
                    .add("type", "text")
                    .add(
                        "sender",
                        Json.createObjectBuilder()
                            .add("name", "0crat")
                    )
                    .add("text", text)
                    .build()
            )
            .back().fetch();
    }

}
