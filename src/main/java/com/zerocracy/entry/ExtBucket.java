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
package com.zerocracy.entry;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.retry.PredefinedRetryPolicies;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.jcabi.s3.Bucket;
import com.jcabi.s3.Region;
import com.jcabi.s3.cached.CdRegion;
import com.jcabi.s3.retry.ReRegion;
import com.zerocracy.Farm;
import com.zerocracy.Item;
import com.zerocracy.Project;
import com.zerocracy.farm.props.Props;
import com.zerocracy.farm.props.PropsFarm;
import java.io.IOException;
import java.util.Collections;
import org.cactoos.Scalar;
import org.cactoos.func.SolidFunc;
import org.cactoos.func.UncheckedFunc;

/**
 * S3 Bucket.
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class ExtBucket implements Scalar<Bucket> {

    /**
     * The singleton.
     */
    private static final UncheckedFunc<Farm, Bucket> SINGLETON =
        new UncheckedFunc<>(
            new SolidFunc<>(
                frm -> {
                    final Props props = new Props(frm);
                    final AmazonS3 aws = AmazonS3ClientBuilder.standard()
                        .withRegion(Regions.US_EAST_1)
                        .withClientConfiguration(
                            new ClientConfiguration()
                                .withRetryPolicy(
                                    PredefinedRetryPolicies.DEFAULT
                                )
                        )
                        .withCredentials(
                            new AWSStaticCredentialsProvider(
                                new BasicAWSCredentials(
                                    props.get("//s3/key"),
                                    props.get("//s3/secret")
                                )
                            )
                        )
                        .build();
                    return new CdRegion(
                        new ReRegion(new Region.Simple(aws))
                    ).bucket(props.get("//s3/bucket"));
                }
            )
        );

    /**
     * The farm.
     */
    private final Farm farm;

    /**
     * Ctor.
     */
    public ExtBucket() {
        this(new PropsFarm(new ExtBucket.DirtyHack()));
    }

    /**
     * Ctor.
     * @param frm The farm
     */
    public ExtBucket(final Farm frm) {
        this.farm = frm;
    }

    @Override
    public Bucket value() {
        return ExtBucket.SINGLETON.apply(this.farm);
    }

    /**
     * It's a workaround for PropsFarm: it requires a farm
     * to access properties, but bucket should be constructed before
     * the farm. So we assume that this bucket factory won't ever access
     * any properties from the farm and that PropsFarm will read all properties
     * for the bucket without accessing other items.
     * @since 1.0
     */
    private static final class DirtyHack implements Farm {

        @Override
        public Iterable<Project> find(final String xpath) {
            return Collections.singleton(new ExtBucket.AnyProject());
        }
    }

    /**
     * Any project.
     */
    private static final class AnyProject implements Project {

        @Override
        public Item acq(final String file) throws IOException {
            throw new IllegalStateException("Should not be called here");
        }
    }
}
