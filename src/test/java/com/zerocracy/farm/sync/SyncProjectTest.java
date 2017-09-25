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
package com.zerocracy.farm.sync;

import com.jcabi.aspects.Tv;
import com.zerocracy.jstk.Item;
import com.zerocracy.jstk.fake.FkProject;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.cactoos.ScalarHasValue;
import org.cactoos.iterable.IterableOf;
import org.cactoos.iterable.Mapped;
import org.cactoos.iterable.Repeated;
import org.cactoos.scalar.And;
import org.hamcrest.MatcherAssert;
import org.junit.Test;

/**
 * Test case for {@link SyncProject}.
 * @author Kirill (g4s8.public@gmail.com)
 * @version $Id$
 * @since 0.12
 * @checkstyle JavadocMethodCheck (500 lines)
 */
public final class SyncProjectTest {

    @Test
    public void cleanUp() throws Exception {
        final int threshold = 1;
        final Map<String, SyncItem> pool = new HashMap<>();
        final SyncProject project = new SyncProject(
            new FkProject(),
            pool,
            threshold
        );
        new And(
            new Mapped<>(
                new IterableOf<>("one", "two", "three"),
                project::acq
            ),
            Item::close
        ).value();
        MatcherAssert.assertThat(
            "SyncProject did not clean item's pool",
            new And(
                new Repeated<>(0, Tv.TWENTY),
                x -> {
                    TimeUnit.MILLISECONDS.sleep(1L);
                    return pool.size() != threshold;
                }
            ),
            new ScalarHasValue<>(false)
        );
    }
}
