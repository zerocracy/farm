/*
 * Copyright (c) 2016-2019 Zerocracy
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
package com.zerocracy.tk;

import com.zerocracy.Farm;
import com.zerocracy.health.HealthChecks;
import java.io.IOException;
import org.cactoos.scalar.IoCheckedScalar;
import org.takes.Request;
import org.takes.Response;
import org.takes.Take;
import org.takes.rs.xe.XeAppend;
import org.takes.rs.xe.XeChain;

/**
 * Health page.
 *
 * @since 1.0
 */
public final class TkHealth implements Take {

    /**
     * S3 check name.
     */
    private static final String AWS_S3 = "aws-s3";

    /**
     * SQS check name.
     */
    private static final String AWS_SQS = "aws-sqs";

    /**
     * Github check name.
     */
    private static final String GITHUB = "github";

    /**
     * Brigade check name.
     */
    private static final String BRIGADE = "brigade";

    /**
     * Farm.
     */
    private final Farm farm;

    /**
     * Ctor.
     * @param farm Farm
     */
    public TkHealth(final Farm farm) {
        this.farm = farm;
    }

    @Override
    public Response act(final Request req) throws IOException {
        final HealthChecks checks =
            new IoCheckedScalar<>(new HealthChecks.Ext(this.farm)).value();
        return new RsPage(
            this.farm, "/xsl/health.xsl", req,
            () -> new XeChain(
                new XeAppend(
                    TkHealth.AWS_S3, checks.status(TkHealth.AWS_S3)
                ),
                new XeAppend(
                    TkHealth.AWS_SQS, checks.status(TkHealth.AWS_SQS)
                ),
                new XeAppend(
                    TkHealth.GITHUB, checks.status(TkHealth.GITHUB)
                ),
                new XeAppend(
                    TkHealth.BRIGADE, checks.status(TkHealth.BRIGADE)
                )
            )
        );
    }
}
