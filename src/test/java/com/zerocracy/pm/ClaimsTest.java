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
package com.zerocracy.pm;

import com.jcabi.aspects.Tv;
import com.jcabi.log.VerboseThreads;
import com.jcabi.s3.Bucket;
import com.jcabi.s3.fake.FkBucket;
import com.zerocracy.farm.S3Farm;
import com.zerocracy.farm.sync.SyncFarm;
import com.zerocracy.jstk.Item;
import com.zerocracy.jstk.Project;
import com.zerocracy.jstk.fake.FkProject;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.cactoos.Func;
import org.cactoos.io.LengthOf;
import org.cactoos.io.TeeInput;
import org.cactoos.scalar.And;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link Claims}.
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.9
 * @checkstyle JavadocMethodCheck (500 lines)
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class ClaimsTest {

    @Test
    public void modifiesInMultipleThreads() throws Exception {
        final Bucket bucket = new FkBucket(
            Files.createTempDirectory("").toFile(),
            "the-bucket"
        );
        final Project project = new SyncFarm(new S3Farm(bucket))
            .find("@id='ABCZZFE03'").iterator().next();
        final int threads =
            Runtime.getRuntime().availableProcessors() << Tv.FOUR;
        final ExecutorService service = Executors.newFixedThreadPool(
            threads, new VerboseThreads()
        );
        final CountDownLatch latch = new CountDownLatch(1);
        final Collection<Future<Boolean>> futures = new ArrayList<>(threads);
        final Callable<Boolean> task = () -> {
            latch.await();
            new ClaimOut().type("how are you").postTo(project);
            return true;
        };
        for (int thread = 0; thread < threads; ++thread) {
            futures.add(service.submit(task));
        }
        latch.countDown();
        MatcherAssert.assertThat(
            new And(
                futures,
                (Func<Future<Boolean>, Boolean>) Future::get
            ).value(),
            Matchers.is(true)
        );
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
                        "xsi:noNamespaceSchemaLocation='https://raw.githubusercontent.com/zerocracy/datum/0.27/xsd/pm/claims.xsd'",
                        "version='0.1' updated='2017-03-27T11:18:09.228Z'/>"
                    ),
                    item.path()
                )
            ).value();
        }
        final Claims claims = new Claims(project).bootstrap();
        claims.add(new ClaimOut().token("test;test;1").type("just hello"));
        MatcherAssert.assertThat(
            claims.iterate().iterator().hasNext(),
            Matchers.is(true)
        );
    }

    @Test
    public void addsAndRemovesClaims() throws Exception {
        final Claims claims = new Claims(new FkProject()).bootstrap();
        claims.add(new ClaimOut().token("test;test").type("hello"));
        MatcherAssert.assertThat(
            claims.iterate().iterator().next().xpath("token/text()").get(0),
            Matchers.startsWith("test;")
        );
    }

}
