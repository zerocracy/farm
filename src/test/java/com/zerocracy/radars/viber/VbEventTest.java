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

import java.time.Instant;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Tests for {@link VbEvent}.
 *
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class VbEventTest {

    @Test
    public void parsesSimply() {
        try (
            final JsonReader reader = Json.createReader(
                VbEventTest.class.getResourceAsStream("message.json")
            )
        ) {
            final JsonObject json = reader.readObject();
            final VbEvent event = new VbEvent.Simple(json);
            VbEventTest.validatesBasics(json, event);
        }
    }

    @Test
    public void parsesMessage() {
        try (
            final JsonReader reader = Json.createReader(
                VbEventTest.class.getResourceAsStream("message.json")
            )
        ) {
            final JsonObject json = reader.readObject();
            final VbEvent.Message event = new VbEvent.Message(
                new VbEvent.Simple(json)
            );
            VbEventTest.validatesBasics(json, event);
            MatcherAssert.assertThat(event.vid(), Matchers.is("01234567890A="));
            MatcherAssert.assertThat(
                event.message(), Matchers.is("a message to the service")
            );
        }
    }

    private static void validatesBasics(final JsonObject json,
        final VbEvent event) {
        MatcherAssert.assertThat(event.event(), Matchers.is("message"));
        MatcherAssert.assertThat(
            event.timestamp(),
            // @checkstyle MagicNumber (1 line)
            Matchers.is(Instant.ofEpochMilli(1457764197627L))
        );
        MatcherAssert.assertThat(
            event.token(), Matchers.is("4912661846655238145")
        );
        MatcherAssert.assertThat(event.json(), Matchers.is(json));
    }

}
