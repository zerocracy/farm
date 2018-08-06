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
package com.zerocracy.pm;

import com.jcabi.aspects.Tv;
import com.jcabi.s3.Bucket;
import com.jcabi.s3.fake.FkBucket;
import com.zerocracy.Farm;
import com.zerocracy.Item;
import com.zerocracy.Project;
import com.zerocracy.RunsInThreads;
import com.zerocracy.Xocument;
import com.zerocracy.claims.ClaimOut;
import com.zerocracy.claims.ClaimsItem;
import com.zerocracy.entry.ClaimsOf;
import com.zerocracy.farm.S3Farm;
import com.zerocracy.farm.fake.FkProject;
import com.zerocracy.farm.sync.SyncFarm;
import java.nio.file.Files;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import org.cactoos.io.LengthOf;
import org.cactoos.io.TeeInput;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Ignore;
import org.junit.Test;
import org.xembly.Directives;

/**
 * Test case for {@link ClaimsItem}.
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class ClaimsItemTest {

    @Test
    @Ignore
    public void modifiesInMultipleThreads() throws Exception {
        final Bucket bucket = new FkBucket(
            Files.createTempDirectory("").toFile(),
            "the-bucket"
        );
        try (final Farm farm = new SyncFarm(new S3Farm(bucket))) {
            final Project project = farm.find("@id='ABCZZFE03'")
                .iterator().next();
            MatcherAssert.assertThat(
                input -> {
                    new ClaimOut()
                        .type("how are you")
                        .param("something", input.incrementAndGet())
                        .postTo(new ClaimsOf(farm, project));
                    return true;
                },
                new RunsInThreads<>(new AtomicInteger())
            );
        }
    }

    @Test
    public void opensExistingClaimsXml() throws Exception {
        final Project project = new FkProject();
        try (final Item item = project.acq("claims.xml")) {
            new LengthOf(
                new TeeInput(
                    String.join(
                        " ",
                        "<claims",
                        "xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'",
                        // @checkstyle LineLength (1 line)
                        "xsi:noNamespaceSchemaLocation='http://datum.zerocracy.com/0.27/xsd/pm/claims.xsd'",
                        "version='0.1' updated='2017-03-27T11:18:09.228Z'/>"
                    ),
                    item.path()
                )
            ).intValue();
        }
        final ClaimsItem claims = new ClaimsItem(project).bootstrap();
        claims.add(new ClaimOut().token("test;test;1").type("just hello"));
        MatcherAssert.assertThat(
            claims.iterate().iterator().hasNext(),
            Matchers.is(true)
        );
    }

    @Test
    public void addsAndRemovesClaims() throws Exception {
        final ClaimsItem claims = new ClaimsItem(new FkProject()).bootstrap();
        claims.add(new ClaimOut().token("test;test").type("Hello"));
        MatcherAssert.assertThat(
            claims.iterate().iterator().next().xpath("token/text()").get(0),
            Matchers.startsWith("test;")
        );
    }

    @Test
    public void ignoresClaimsUntilTheyBecomeValid() throws Exception {
        final ClaimsItem claims = new ClaimsItem(new FkProject()).bootstrap();
        claims.add(
            new ClaimOut()
                .until(Duration.ofMinutes(1L))
                .type("hello future")
        );
        MatcherAssert.assertThat(
            claims.iterate().iterator().hasNext(),
            Matchers.equalTo(false)
        );
    }

    @Test
    public void ignoreDuplicateClaims() throws Exception {
        final ClaimsItem claims = new ClaimsItem(new FkProject()).bootstrap();
        final String type = "hello future";
        claims.add(new ClaimOut().type(type));
        claims.add(new ClaimOut().type(type));
        MatcherAssert.assertThat(
            claims.iterate(),
            Matchers.iterableWithSize(1)
        );
    }

    @Test
    @Ignore
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public void iteratesLargeSetOfClaims() throws Exception {
        final Project project = new FkProject();
        final int total = Tv.HUNDRED;
        try (final Item item = project.acq("claims.xml")) {
            final Xocument doc = new Xocument(item).bootstrap("pm/claims");
            for (int idx = 0; idx < total; ++idx) {
                doc.modify(
                    new Directives().xpath("/claims").append(
                        new ClaimOut().type("hello my future")
                    )
                );
            }
        }
        MatcherAssert.assertThat(
            new ClaimsItem(project).bootstrap().iterate().size(),
            Matchers.equalTo(total)
        );
    }

}
