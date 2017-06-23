/**
 * Copyright (c) 2016-2017 Zerocracy
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
package com.zerocracy;

import com.icegreen.greenmail.base.GreenMailOperations;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.AbstractMap;
import org.cactoos.Func;
import org.cactoos.func.FuncAsMatcher;
import org.cactoos.func.RetryScalar;
import org.cactoos.io.InputAsProperties;
import org.cactoos.list.MapAsProperties;
import org.cactoos.text.BytesAsText;
import org.cactoos.text.ThrowableAsBytes;
import org.hamcrest.MatcherAssert;
import org.junit.Test;

/**
 * Test case for {@link ThrowableToEmail}.
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.11
 * @checkstyle JavadocMethodCheck (500 lines)
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class ThrowableToEmailTest {

    @Test
    public void throwsIfInTestingMode() throws Exception {
        MatcherAssert.assertThat(
            new ThrowableToEmail(
                new MapAsProperties(
                    new AbstractMap.SimpleEntry<>(
                        "testing", "true"
                    )
                ).asValue()
            ),
            new FuncAsMatcher<>(
                (Func<Func<Throwable, Boolean>, Boolean>) func -> {
                    try {
                        func.apply(new IOException("hello, world!"));
                        throw new AssertionError("Exception expected");
                    } catch (final IllegalStateException ex) {
                        return new BytesAsText(
                            new ThrowableAsBytes(ex)
                        ).asString().contains("hello");
                    }
                }
            )
        );
    }

    @Test
    public void sendsEmailToRealSmtpServer() throws Exception {
        final int port = new RetryScalar<>(
            ThrowableToEmailTest::reserve
        ).asValue();
        final GreenMailOperations mail = new GreenMail(
            new ServerSetup(port, null, ServerSetup.PROTOCOL_SMTP)
        );
        mail.start();
        MatcherAssert.assertThat(
            new ThrowableToEmail(
                new InputAsProperties(
                    String.join(
                        "\n",
                        "smtp.protocol=smtp",
                        "smtp.username=test",
                        "smtp.password=test",
                        "smtp.host=127.0.0.1",
                        String.format("smtp.port=%d", port)
                    )
                ).asValue()
            ),
            new FuncAsMatcher<>(
                (Func<Func<Throwable, Boolean>, Boolean>) func -> {
                    try {
                        func.apply(new IOException("hey you!"));
                        throw new AssertionError("Exception expected here");
                    } catch (final IllegalStateException ex) {
                        return new BytesAsText(
                            new ThrowableAsBytes(ex)
                        ).asString().contains("you");
                    }
                }
            )
        );
        mail.stop();
    }

    /**
     * Reserve port.
     * @return Reserved TCP port
     * @throws IOException If fails
     */
    private static int reserve() throws IOException {
        final int reserved;
        try (final ServerSocket socket = new ServerSocket(0)) {
            reserved = socket.getLocalPort();
        }
        return reserved;
    }

}
