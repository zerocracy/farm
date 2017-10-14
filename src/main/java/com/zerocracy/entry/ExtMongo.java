/**
 * Copyright (c) 2016-2017 Zerocracy
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
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.zerocracy.farm.props.Props;
import com.zerocracy.jstk.Farm;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import org.cactoos.Scalar;
import org.cactoos.func.SyncFunc;
import org.cactoos.func.UncheckedFunc;
import org.cactoos.list.ListOf;
import org.cactoos.scalar.StickyScalar;
import org.cactoos.scalar.SyncScalar;
import org.cactoos.scalar.UncheckedScalar;

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
     * The singleton.
     */
    private static final UncheckedFunc<Farm, MongoClient> SINGLETON =
        new UncheckedFunc<>(
            new SyncFunc<>(
                frm -> {
                    final Props props = new Props(frm);
                    final MongoClient client;
                    if (props.has("//testing")) {
                        client = new MongoClient(
                            "localhost", ExtMongo.FAKE.value()
                        );
                    } else {
                        client = new MongoClient(
                            new ServerAddress(
                                props.get("//mongo/host"),
                                Integer.parseInt(props.get("//mongo/port"))
                            ),
                            new ListOf<>(
                                MongoCredential.createCredential(
                                    props.get("//mongo/user"),
                                    "admin",
                                    props.get("//mongo/password").toCharArray()
                                )
                            )
                        );
                    }
                    return client;
                }
            )
        );

    /**
     * Thread with Mongodb.
     * @checkstyle ConstantUsageCheck (5 lines)
     */
    private static final UncheckedScalar<Integer> FAKE = new UncheckedScalar<>(
        new SyncScalar<>(
            new StickyScalar<>(
                () -> {
                    final ServerSocket socket = new ServerSocket();
                    final int port;
                    try {
                        socket.setReuseAddress(true);
                        socket.bind(new InetSocketAddress("localhost", 0));
                        port = socket.getLocalPort();
                    } finally {
                        socket.close();
                    }
                    final Path dir = Files.createTempDirectory("mongo-log");
                    final Process mongod = new ProcessBuilder()
                        .command(
                            "mongod",
                            "--quiet",
                            "--logpath",
                            dir.resolve("log.txt").toAbsolutePath().toString(),
                            "--dbpath",
                            Files.createTempDirectory("ft").toString(),
                            "--port",
                            Integer.toString(port)
                        )
                        .redirectInput(ProcessBuilder.Redirect.INHERIT)
                        .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                        .redirectError(ProcessBuilder.Redirect.INHERIT)
                        .start();
                    Runtime.getRuntime().addShutdownHook(
                        new Thread(mongod::destroy)
                    );
                    return port;
                }
            )
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
    public MongoClient value() {
        return ExtMongo.SINGLETON.apply(this.farm);
    }

}
