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
package com.zerocracy.entry;

import com.amazonaws.services.sqs.AmazonSQS;
import com.zerocracy.Farm;
import com.zerocracy.Item;
import com.zerocracy.Project;
import com.zerocracy.farm.fake.FkFarm;
import com.zerocracy.farm.fake.FkProject;
import org.cactoos.io.LengthOf;
import org.cactoos.io.TeeInput;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Ignore;
import org.junit.Test;
import org.xembly.Directives;
import org.xembly.Xembler;

/**
 * Test case for {@link ClaimsOf}.
 * <p>
 * To start this test you have to create AWS user with full access to
 * SQS queues policy and insert credentials to SQS_KEY and SQS_SECRET
 * fields.
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 * @checkstyle JavadocMethodCheck (500 lines)
 */
@Ignore
public final class ClaimsOfITCase {

    /**
     * SQS AWS key.
     */
    private static final String SQS_KEY = "";

    /**
     * SQS AWS secret.
     */
    private static final String SQS_SECRET = "";

    @Test
    public void createsNewQueue() throws Exception {
        final Project project = new FkProject();
        try (final Item item = project.acq("_props.xml")) {
            new LengthOf(
                new TeeInput(
                    new Xembler(
                        new Directives()
                            .add("props")
                            .add("sqs")
                            .add("key").set(ClaimsOfITCase.SQS_KEY).up()
                            .add("secret").set(ClaimsOfITCase.SQS_SECRET).up()
                    ).xmlQuietly(),
                    item.path()
                )
            ).value();
        }
        final Farm farm = new FkFarm(project);
        new ClaimsOf(farm).take(
            xml -> {
            },
            1
        );
        final AmazonSQS sqs = new ExtSqs(farm).value();
        final String url = sqs.getQueueUrl(
            String.format("project-%s", project.pid())
        ).getQueueUrl();
        sqs.deleteQueue(url);
        MatcherAssert.assertThat(
            url,
            Matchers.not(Matchers.isEmptyOrNullString())
        );
    }
}
