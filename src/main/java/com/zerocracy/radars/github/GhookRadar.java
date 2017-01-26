/**
 * Copyright (c) 2016 Zerocracy
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
package com.zerocracy.radars.github;

import com.jcabi.github.Github;
import com.zerocracy.jstk.Farm;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import javax.json.Json;
import javax.json.stream.JsonParsingException;
import org.takes.Request;
import org.takes.Response;
import org.takes.Take;
import org.takes.rq.form.RqFormBase;
import org.takes.rq.form.RqFormSmart;
import org.takes.rs.RsText;
import org.takes.rs.RsWithStatus;

/**
 * GitHub hook.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.7
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class GhookRadar implements Take {

    /**
     * Reaction.
     */
    private final Rebound rebound;

    /**
     * Farm.
     */
    private final Farm farm;

    /**
     * Github client.
     */
    private final Github github;

    /**
     * Ctor.
     * @param frm Farm
     * @param ghb Github
     * @param rbd Rebound
     */
    public GhookRadar(final Farm frm, final Github ghb, final Rebound rbd) {
        this.farm = frm;
        this.github = ghb;
        this.rebound = rbd;
    }

    @Override
    public Response act(final Request req) throws IOException {
        final String body = new RqFormSmart(
            new RqFormBase(req)
        ).single("payload");
        try {
            return new RsWithStatus(
                new RsText(
                    this.rebound.react(
                        this.farm,
                        this.github,
                        Json.createReader(
                            new ByteArrayInputStream(
                                body.getBytes(StandardCharsets.UTF_8)
                            )
                        ).readObject()
                    )
                ),
                HttpURLConnection.HTTP_OK
            );
        } catch (final JsonParsingException ex) {
            throw new IllegalArgumentException(
                String.format("Can't parse JSON: %s", body),
                ex
            );
        }
    }

}
