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
package com.zerocracy.farm.footprint;

import com.mongodb.MongoClient;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.file.Files;
import org.cactoos.func.RunnableOf;
import org.cactoos.scalar.StickyScalar;
import org.cactoos.scalar.SyncScalar;
import org.cactoos.scalar.UncheckedScalar;

/**
 * Fake instance of MongoDB.
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.18
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class FakeMongo implements AutoCloseable {

    /**
     * Thread with Mongodb.
     */
    private final UncheckedScalar<Thread> daemon = new UncheckedScalar<>(
        new SyncScalar<>(
            new StickyScalar<>(
                () -> new Thread(
                    new RunnableOf<>(
                        input -> {
                            new ProcessBuilder().command(
                                "mongod",
                                "--dbpath",
                                Files.createTempDirectory("ft").toString(),
                                "--port",
                                this.port.value().toString()
                            ).redirectErrorStream(true).start();
                        }
                    )
                )
            )
        )
    );

    /**
     * Port of the server.
     */
    private final UncheckedScalar<Integer> port = new UncheckedScalar<>(
        new SyncScalar<>(
            new StickyScalar<>(
                () -> {
                    final ServerSocket socket = new ServerSocket();
                    try  {
                        socket.setReuseAddress(true);
                        socket.bind(new InetSocketAddress("localhost", 0));
                        return socket.getLocalPort();
                    } finally {
                        socket.close();
                    }
                }
            )
        )
    );

    /**
     * Start it.
     * @return Itself
     */
    public FakeMongo start() {
        this.daemon.value().start();
        return this;
    }

    /**
     * Get client.
     * @return The client
     */
    public MongoClient client() {
        return new MongoClient("localhost", this.port.value());
    }

    @Override
    public void close() {
        this.daemon.value().interrupt();
        try {
            this.daemon.value().join();
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(ex);
        }
    }

}
