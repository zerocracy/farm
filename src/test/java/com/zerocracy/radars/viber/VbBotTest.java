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

import com.jcabi.http.mock.MkAnswer;
import com.jcabi.http.mock.MkContainer;
import com.jcabi.http.mock.MkGrizzlyContainer;
import com.jcabi.http.mock.MkQuery;
import java.net.HttpURLConnection;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test for {@link VbBot}.
 *
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 */
public final class VbBotTest {

    @Test
    public void sendsMessage() throws Exception {
        try (
            final MkContainer container = new MkGrizzlyContainer()
                .next(new MkAnswer.Simple(HttpURLConnection.HTTP_OK))
                .start()
        ) {
            final String token =
                "445da6az1s345z78-dazcczb2542zv51a-e0vc5fva17480im9";
            final String receiver = "01234567890A=";
            final String text = "Hello world";
            new VbBot(token, container.home().toString())
                .sendMessage(receiver, text);
            final MkQuery request = container.take();
            MatcherAssert.assertThat(
                request.headers(),
                Matchers.hasEntry(
                    Matchers.is("X-Viber-Auth-Token"), Matchers.contains(token)
                )
            );
            MatcherAssert.assertThat(
                request.body(),
                Matchers.allOf(
                    Matchers.containsString(
                        String.format("\"text\":\"%s\"", text)
                    ),
                    Matchers.containsString(
                        String.format("\"receiver\":\"%s\"", receiver)
                    )
                )
            );
        }
    }

}
