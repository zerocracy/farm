/**
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

import com.zerocracy.farm.fake.FkFarm;
import com.zerocracy.farm.fake.FkProject;
import com.zerocracy.farm.props.PropsFarm;
import com.zerocracy.pm.in.Orders;
import com.zerocracy.pm.scope.Wbs;
import com.zerocracy.pmo.Awards;
import java.io.IOException;
import java.time.LocalDateTime;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link JobSchedule}.
 * @author Kirill (g4s8.public@gmail.com)
 * @version $Id$
 * @since 0.22
 * @checkstyle JavadocMethodCheck (500 lines)
 * @checkstyle MagicNumberCheck (500 lines)
 */
public final class JobScheduleTest {
    @Test
    public void resignDateIncludingExtra() throws IOException {
        final Farm farm = new PropsFarm(new FkFarm());
        final Project pkt = new FkProject();
        final Orders orders = new Orders(pkt).bootstrap();
        final String job = "gh:test/test#1";
        new Wbs(pkt).bootstrap().add(job);
        final String performer = "user2241234";
        orders.assign(job, performer, "test");
        new Awards(farm, performer).bootstrap()
            .add(1300, "gh:none/none#1", "tst");
        MatcherAssert.assertThat(
            new JobSchedule(farm, orders).resign(job).isBefore(
                LocalDateTime.now().plusDays(13L)
            ),
            Matchers.is(true)
        );
    }
}
