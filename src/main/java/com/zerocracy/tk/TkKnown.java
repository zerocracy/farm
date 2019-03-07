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
package com.zerocracy.tk;

import com.zerocracy.Farm;
import com.zerocracy.pmo.People;
import java.io.IOException;
import java.net.HttpURLConnection;
import javax.json.Json;
import org.takes.HttpException;
import org.takes.Response;
import org.takes.facets.fork.RqRegex;
import org.takes.facets.fork.TkRegex;
import org.takes.rs.RsJson;

/**
 * This take returns 200 if user exists, 400 otherwise.
 *
 * @since 1.0
 */
public final class TkKnown implements TkRegex {

    /**
     * Farm.
     */
    private final Farm farm;

    /**
     * Ctor.
     * @param farm Farm
     */
    public TkKnown(final Farm farm) {
        this.farm = farm;
    }

    @Override
    @SuppressWarnings("PMD.AvoidDuplicateLiterals")
    public Response act(final RqRegex req) throws IOException {
        final String login = req.matcher().group("login");
        final People people = new People(this.farm).bootstrap();
        if (!people.hasMentor(login)) {
            throw new HttpException(HttpURLConnection.HTTP_NOT_FOUND);
        }
        return new RsJson(
            Json.createObjectBuilder()
                .add("login", login)
                .add("reputation", people.reputation(login))
                .add("identified", !people.details(login).isEmpty())
                .build()
        );
    }
}
