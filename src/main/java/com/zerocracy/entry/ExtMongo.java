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
package com.zerocracy.entry;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.zerocracy.Farm;
import com.zerocracy.farm.props.Props;
import de.flapdoodle.embed.mongo.Command;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.config.RuntimeConfigBuilder;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.concurrent.TimeUnit;
import org.cactoos.Scalar;
import org.cactoos.list.SolidList;
import org.cactoos.scalar.SolidScalar;
import org.cactoos.scalar.UncheckedScalar;
import org.slf4j.LoggerFactory;

/**
 * MongoDB server connector.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.18
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class ExtMongo implements Scalar<MongoClient> {

    /**
     * Thread with Mongodb.
     * @checkstyle ConstantUsageCheck (5 lines)
     */
    private static final UncheckedScalar<Integer> FAKE = new UncheckedScalar<>(
        new SolidScalar<>(
            () -> {
                final int port;
                try (ServerSocket socket = new ServerSocket()) {
                    socket.setReuseAddress(true);
                    socket.bind(new InetSocketAddress("localhost", 0));
                    port = socket.getLocalPort();
                }
                final MongodStarter starter = MongodStarter.getInstance(
                    new RuntimeConfigBuilder()
                        .defaultsWithLogger(
                            Command.MongoD,
                            LoggerFactory.getLogger(ExtMongo.class)
                        )
                        .build()
                );
                final MongodExecutable executable = starter.prepare(
                    new MongodConfigBuilder()
                        .net(
                            new Net(
                                "localhost",
                                port,
                                Network.localhostIsIPv6()
                            )
                        )
                        .version(Version.Main.V3_5)
                        .build()
                );
                final MongodProcess process = executable.start();
                Runtime.getRuntime().addShutdownHook(
                    new Thread(
                        () -> {
                            process.stop();
                            executable.stop();
                        }
                    )
                );
                return port;
            }
        )
    );

    /**
     * The farm.
     */
    private final Farm farm;

    /**
     * Ctor.
     * @param frm The farm
     */
    public ExtMongo(final Farm frm) {
        this.farm = frm;
    }

    @Override
    public MongoClient value() throws IOException {
        final Props props = new Props(this.farm);
        final MongoClient client;
        if (props.has("//testing")) {
            client = new MongoClient(
                "localhost", ExtMongo.FAKE.value()
            );
        } else {
            // @checkstyle MagicNumber (5 lines)
            final int timeout = (int) TimeUnit.SECONDS.toMillis(15L);
            client = new MongoClient(
                new ServerAddress(
                    props.get("//mongo/host"),
                    Integer.parseInt(props.get("//mongo/port"))
                ),
                new SolidList<>(
                    MongoCredential.createCredential(
                        props.get("//mongo/user"),
                        props.get("//mongo/dbname"),
                        props.get("//mongo/password").toCharArray()
                    )
                ),
                MongoClientOptions.builder()
                    .maxWaitTime(timeout)
                    .socketTimeout(timeout)
                    .connectTimeout(timeout)
                    .serverSelectionTimeout(timeout)
                    .build()
            );
        }
        return client;
    }

}
