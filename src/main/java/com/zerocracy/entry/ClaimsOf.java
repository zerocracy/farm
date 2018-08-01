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
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.QueueDoesNotExistException;
import com.jcabi.xml.XML;
import com.zerocracy.Farm;
import com.zerocracy.Project;
import com.zerocracy.farm.props.Props;
import com.zerocracy.pm.Claims;
import com.zerocracy.pm.ClaimsSqs;
import com.zerocracy.pm.ClaimsXml;
import com.zerocracy.pmo.Pmo;
import java.io.IOException;
import org.cactoos.Proc;
import org.cactoos.func.IoCheckedBiFunc;
import org.cactoos.func.StickyBiFunc;
import org.cactoos.map.MapEntry;
import org.cactoos.map.MapOf;

/**
 * Claims for farm.
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class ClaimsOf implements Claims {

    /**
     * Claims instances.
     */
    private static final IoCheckedBiFunc<Farm, Project, Claims> SINGLETON =
        new IoCheckedBiFunc<>(
            new StickyBiFunc<>(
                (farm, project) -> {
                    final Props props = new Props(farm);
                    final Claims claims;
                    if (props.has("//sqs")) {
                        final AmazonSQS sqs = new ExtSqs(farm).value();
                        final String name =
                            String.format("0crat-%s.fifo", project.pid());
                        String url;
                        try {
                            url = sqs.getQueueUrl(name).getQueueUrl();
                        } catch (final QueueDoesNotExistException ignored) {
                            url = sqs.createQueue(
                                new CreateQueueRequest(name)
                                    .withAttributes(
                                        new MapOf<String, String>(
                                            // @checkstyle LineLength (2 lines)
                                            new MapEntry<>("FifoQueue", Boolean.toString(true)),
                                            new MapEntry<>("ContentBasedDeduplication", Boolean.toString(true))
                                        )
                                    )
                            ).getQueueUrl();
                        }
                        claims = new ClaimsSqs(sqs, url, project);
                    } else {
                        claims = new ClaimsXml(project);
                    }
                    return new ValidClaims(claims);
                }
            )
        );

    /**
     * Farm.
     */
    private final Farm frm;

    /**
     * Project.
     */
    private final Project proj;

    /**
     * Ctor for PMO.
     *
     * @param farm Farm
     */
    public ClaimsOf(final Farm farm) {
        this(farm, new Pmo(farm));
    }

    /**
     * Ctor.
     *
     * @param farm Farm
     * @param project Project
     */
    public ClaimsOf(final Farm farm, final Project project) {
        this.frm = farm;
        this.proj = project;
    }

    @Override
    public void take(final Proc<XML> proc, final int limit) throws IOException {
        ClaimsOf.SINGLETON.apply(this.frm, this.proj).take(proc, limit);
    }

    @Override
    public void submit(final XML claim) throws IOException {
        ClaimsOf.SINGLETON.apply(this.frm, this.proj).submit(claim);
    }
}
