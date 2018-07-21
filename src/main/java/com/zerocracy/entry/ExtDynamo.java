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

import com.jcabi.dynamo.Credentials;
import com.jcabi.dynamo.Region;
import com.jcabi.dynamo.mock.H2Data;
import com.jcabi.dynamo.mock.MkRegion;
import com.jcabi.dynamo.retry.ReRegion;
import com.zerocracy.Farm;
import com.zerocracy.farm.props.Props;
import com.zerocracy.farm.props.PropsFarm;
import org.cactoos.Scalar;
import org.cactoos.func.SolidFunc;
import org.cactoos.func.UncheckedFunc;

/**
 * DynamoDB server connector.
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class ExtDynamo implements Scalar<Region> {

    /**
     * The singleton.
     */
    private static final UncheckedFunc<Farm, Region> SINGLETON =
        new UncheckedFunc<>(
            new SolidFunc<>(
                frm -> {
                    final Props props = new Props(frm);
                    final Region region;
                    if (props.has("//testing")) {
                        final String port = System.getProperty(
                            "dynamo.port", ""
                        );
                        if (port.isEmpty()) {
                            region = new MkRegion(
                                new H2Data()
                                    .with(
                                        "0crat-hints",
                                        new String[] {"mnemo"},
                                        "when", "ttl"
                                    )
                            );
                        } else {
                            region = new Region.Simple(
                                new Credentials.Direct(
                                    Credentials.TEST, Integer.parseInt(port)
                                )
                            );
                        }
                    } else {
                        region = new ReRegion(
                            new Region.Simple(
                                new Credentials.Simple(
                                    props.get("//dynamo/key"),
                                    props.get("//dynamo/secret")
                                )
                            )
                        );
                    }
                    return region;
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
    public ExtDynamo() {
        this(new PropsFarm());
    }

    /**
     * Ctor.
     * @param frm The farm
     */
    public ExtDynamo(final Farm frm) {
        this.farm = frm;
    }

    @Override
    public Region value() {
        return ExtDynamo.SINGLETON.apply(this.farm);
    }
}
