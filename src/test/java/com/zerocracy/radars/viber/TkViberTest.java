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

import com.zerocracy.farm.fake.FkFarm;
import java.io.StringReader;
import javax.json.Json;
import org.cactoos.text.TextOf;
import org.junit.Test;
import org.mockito.Mockito;
import org.takes.rq.RqFake;
import org.takes.rq.RqWithBody;

/**
 * Tests for {@link TkViber}.
 *
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class TkViberTest {
    @Test(expected = IllegalArgumentException.class)
    public void failsForEmptyBody() throws Exception {
        new TkViber(new FkFarm(), new VbBot("token")).act(
            new RqWithBody(
                new RqFake("POST", "/"), ""
            )
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void failsForInvalidJson() throws Exception {
        new TkViber(new FkFarm(), new VbBot("token")).act(
            new RqWithBody(
                new RqFake("POST", "/"), "{test"
            )
        );
    }

    @Test
    public void reactsToMessage() throws Exception {
        final Reaction reaction = Mockito.mock(Reaction.class);
        final FkFarm farm = new FkFarm();
        final VbBot bot = new VbBot("token");
        final String callback = new TextOf(
            TkViberTest.class.getResourceAsStream("message.json")
        ).asString();
        new TkViber(farm, bot, reaction).act(
            new RqWithBody(
                new RqFake("POST", "/"),
                callback
            )
        );
        Mockito.verify(reaction)
            .react(
                Mockito.eq(bot), Mockito.eq(farm), Mockito.argThat(
                    event -> event.json()
                        .equals(
                            Json.createReader(new StringReader(callback))
                                .readObject()
                        )
                )
            );
    }

}
