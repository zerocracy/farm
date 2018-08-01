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
import com.zerocracy.Project;
import com.zerocracy.farm.fake.FkFarm;
import com.zerocracy.farm.fake.FkProject;
import com.zerocracy.farm.props.Props;
import com.zerocracy.farm.props.PropsFarm;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test case for {@link ClaimsOf}.
 * <p>
 * To start this test you have to create AWS user with full access to
 * SQS queues policy and insert credentials to test {@code _props.xml}:
 * <pre><code>
 * &lt;props&gt;
 *     &lt;sqs&gt;
 *         &lt;key&gt;your-key&lt;/key&gt;
 *         &lt;secret&gt;your-secret&lt;/secret&gt;
 *     &lt;/sqs&gt;
 * &lt;/props&gt;
 * </code></pre>
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 * @checkstyle JavadocStyleCheck (500 lines)
 * @checkstyle JavadocMethodCheck (500 lines)
 */
public final class ClaimsOfITCase {

    @BeforeClass
    public static void checkProps() throws Exception {
        Assume.assumeTrue(
            "sqs credentials are not provided",
            new Props(new PropsFarm()).has("//sqs")
        );
    }

    @Test
    public void createsNewQueue() throws Exception {
        final Project project = new FkProject();
        final Farm farm = new PropsFarm(new FkFarm(project));
        new ClaimsOf(farm).take(
            xml -> {
            },
            1
        );
        final AmazonSQS sqs = new ExtSqs(farm).value();
        final String url = sqs.getQueueUrl(
            String.format("0crat-%s.fifo", project.pid())
        ).getQueueUrl();
        sqs.deleteQueue(url);
        MatcherAssert.assertThat(
            url,
            Matchers.not(Matchers.isEmptyOrNullString())
        );
    }
}
