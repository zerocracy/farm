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
package com.zerocracy;

import com.jcabi.aspects.Tv;
import com.jcabi.matchers.XhtmlMatchers;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Phaser;
import java.util.concurrent.TimeUnit;
import org.cactoos.io.LengthOf;
import org.cactoos.io.TeeInput;
import org.cactoos.text.JoinedText;
import org.cactoos.text.TextOf;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.xembly.Directives;

/**
 * Test case for {@link Xocument}.
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class XocumentTest {

    @Test
    public void upgradesXmlDocument() throws Exception {
        final Path temp = Files.createTempFile("xocument", ".xml");
        new LengthOf(
            new TeeInput(
                new JoinedText(
                    " ",
                    "<catalog version='0.32.2'",
                    "xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'",
                    "xsi:noNamespaceSchemaLocation=",
                    "'http://datum.zerocracy.com/0.32.3/xsd/pmo/catalog.xsd'>",
                    "<project id='ABCDEFGHT'>",
                    "<fee>$5</fee>",
                    "<created>2017-01-02T12:00:00</created></project>",
                    "<project id='ABCDEFGHI'>",
                    "<created>2017-01-01T12:00:00</created></project>",
                    "</catalog>"
                ),
                temp
            )
        ).intValue();
        MatcherAssert.assertThat(
            new Xocument(temp)
                .bootstrap("pmo/catalog")
                .nodes("/catalog/project"),
            Matchers.hasSize(2)
        );
        MatcherAssert.assertThat(
            new TextOf(temp).asString(),
            XhtmlMatchers.hasXPath("/catalog/project/publish")
        );
    }

    // @todo #1037:30min Xocument is not thread safe. Because of this, multiple
    //  threads modifying the same file can result in race conditions. Let's
    //  fix the concurrency issue, then enable this unit test.
    @Test
    @org.junit.Ignore
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public void concurrentlyModifiesXml() throws Exception {
        final Path temp = Files.createTempFile("concurrent", "test.xml");
        final Xocument xocument = new Xocument(temp)
            .bootstrap("pm/staff/roles");
        final int threads = Tv.HUNDRED;
        final Phaser phaser = new Phaser(threads);
        final CountDownLatch end = new CountDownLatch(threads);
        for (int thread = 0; thread < threads; ++thread) {
            final String id = String.format("thread%s", thread);
            new Thread(
                () -> {
                    phaser.arriveAndAwaitAdvance();
                    xocument.modify(
                        new Directives().xpath("/roles")
                            .add("person")
                            .attr("id", id)
                            .addIf("role").set("DEV")
                    );
                    end.countDown();
                }
            ).start();
        }
        end.await(Tv.TEN, TimeUnit.MINUTES);
        MatcherAssert.assertThat(
            xocument.nodes("/roles/person"),
            Matchers.hasSize(threads)
        );
    }

}
