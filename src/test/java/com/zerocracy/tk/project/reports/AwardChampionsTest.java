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
package com.zerocracy.tk.project.reports;

import com.jcabi.xml.XML;
import com.zerocracy.Project;
import com.zerocracy.claims.ClaimOut;
import com.zerocracy.claims.ClaimsItem;
import com.zerocracy.claims.Footprint;
import com.zerocracy.entry.ClaimsOf;
import com.zerocracy.farm.fake.FkProject;
import com.zerocracy.farm.props.PropsFarm;
import java.time.Instant;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link AwardChampions}.
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class AwardChampionsTest {

    @Test
    public void retrievesReportData() throws Exception {
        final Project pkt = new FkProject("7460928I9");
        final long points = 15L;
        new ClaimOut().type("Award points were added")
            .param("login", "yegor256")
            .param("points", points)
            .postTo(new ClaimsOf(new PropsFarm(), pkt));
        final XML xml = new ClaimsItem(pkt).iterate().iterator().next();
        try (final Footprint footprint = new Footprint(new PropsFarm(), pkt)) {
            footprint.open(xml, "test");
            MatcherAssert.assertThat(
                footprint.collection().aggregate(
                    new AwardChampions().bson(
                        pkt,
                        Instant.ofEpochMilli(0L),
                        Instant.now()
                    )
                ).iterator().next().get("total"),
                Matchers.equalTo(points)
            );
        }
    }

}
