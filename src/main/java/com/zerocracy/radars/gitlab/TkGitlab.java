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
package com.zerocracy.radars.gitlab;

import com.zerocracy.Par;
import com.zerocracy.tk.RsParFlash;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
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
 * GitLab hook, take.
 *
 * @since 1.0
 * @todo #1144:30min Create an implementation of GitLab API that will mirror
 *  the one we use for Github (Github class and friends). We only need those
 *  API endpoints that we use in farm project so don't try to implement the
 *  full API.
 * @todo #1144:30min Create a generic interface for hosted version control
 *  systems that we could use in stakeholders (and other classes) where we
 *  don't care about specific vendor (Github or Gitlab). Remember that besides
 *  the generic interface stakeholders have to have a way to find out if they
 *  are working with GitLab or GitHub. Most probably React and Rebound
 *  interfaces should be reworked to support such general vendor.
 * @todo #1144:30min Mimic the functionality of TkGithub in this class. For
 *  Gitlab we should support all the webhook notifications that we currently do
 *  for Github.
 */
public final class TkGitlab implements Take {

    @Override
    public Response act(final Request req) throws IOException {
        if (
            TkGitlab.json(
                new InputStreamReader(req.body(), StandardCharsets.UTF_8)
            ).isEmpty()
        ) {
            throw new RsForward(
                new RsParFlash(
                    new Par(
                        "We expect this URL to be called by GitLab",
                        "with JSON as body of POST"
                    ).say(),
                    Level.WARNING
                )
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
