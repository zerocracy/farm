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

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
import com.zerocracy.Farm;
import java.io.IOException;
import org.cactoos.Scalar;
import org.cactoos.func.IoCheckedFunc;
import org.cactoos.func.SolidFunc;

/**
 * CloudWatch client.
 *
 * @since 1.0
 */
public final class ExtCloudWatch implements Scalar<AmazonCloudWatch> {

    /**
     * Instances.
     */
    private static final IoCheckedFunc<Farm, AmazonCloudWatch> INSTANCES =
        new IoCheckedFunc<>(
            new SolidFunc<>(
                frm -> AmazonCloudWatchClient.builder()
                    .withCredentials(
                        new AWSStaticCredentialsProvider(
                            new PropsAwsCredentials(frm, "cloudwatch")
                        )
                    ).withRegion(Regions.US_EAST_1)
                    .build()
            )
        );

    /**
     * Farm.
     */
    private final Farm farm;

    /**
     * Ctor.
     *
     * @param farm Farm
     */
    public ExtCloudWatch(final Farm farm) {
        this.farm = farm;
    }

    @Override
    public AmazonCloudWatch value() throws IOException {
        return ExtCloudWatch.INSTANCES.apply(this.farm);
    }
}
