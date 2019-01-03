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
import org.cactoos.func.SolidFunc;
import org.cactoos.func.UncheckedFunc;
import org.slf4j.LoggerFactory;

/**
 * MongoDB server connector.
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 * @todo #1711:30min Provide a single Mongo database instance (process) for
 *  all tests. The ExtMongo launches a new Mongo database process every time
 *  it is requested for the testing purposes.
 * @todo #1711:30min Provide a documentation on how to configure an external
 *  (local or remote) Mongo database authorization. It is not clear how to do
 *  this properly. If a Mongo database security.authorization option is turned
 *  on, it is impossible to pass the authentication by running associated unit
 *  tests.
 * @todo #1711:30min Fix up failing cases when an external Mongo database is
 *  specified for tests. Under these conditions the Mongo database data is not
 *  get cleared every time a new database related test is started.
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class ExtMongo implements Scalar<MongoClient> {

    /**
     * The test host property name.
     */
    private static final String TEST_HOST_PROPERTY = "test.mongo.host";

    /**
     * The test port property name.
     */
    private static final String TEST_PORT_PROPERTY = "test.mongo.port";

    /**
     * The test user property name.
     */
    private static final String TEST_USER_PROPERTY = "test.mongo.user";

    /**
     * The test password property name.
     */
    private static final String TEST_PASSWORD_PROPERTY = "test.mongo.password";

    /**
     * The test database name property name.
     */
    private static final String TEST_DB_NAME_PROPERTY = "test.mongo.dbname";

    /**
     * The default password.
     */
    private static final String DEFAULT_PASSWORD = "";

    /**
     * The default database name.
     */
    private static final String DEFAULT_DB_NAME = "footprint";

    /**
     * The default timeout.
     * @checkstyle MagicNumberCheck (1 lines)
     */
    private static final int DEFAULT_TIMEOUT =
        (int) TimeUnit.SECONDS.toMillis(15L);

    /**
     * Thread with Mongodb.
     * @checkstyle ConstantUsageCheck (1 lines)
     */
    private static final UncheckedFunc<String, Integer> FAKE =
        new UncheckedFunc<>(
        new SolidFunc<>(
            (id) -> {
                final int port;
                try (ServerSocket socket = new ServerSocket()) {
                    socket.setReuseAddress(true);
                    socket.bind(
                        new InetSocketAddress(
                            ServerAddress.defaultHost(),
                            0
                        )
                    );
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
                                ServerAddress.defaultHost(),
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
     * Mongo instance identifier.
     * Used to help with tests that depend on current mongo state.
     */
    private final String id;

    /**
     * Ctor.
     * @param frm The farm
     */
    public ExtMongo(final Farm frm) {
        this(frm, "");
    }

    /**
     * Ctor.
     * @param frm The farm
     * @param ident Mongo instance identifier
     */
    public ExtMongo(final Farm frm, final String ident) {
        this.farm = frm;
        this.id = ident;
    }

    @Override
    public MongoClient value() throws IOException {
        final Props props = new Props(this.farm);
        final MongoClient client;
        if (props.has("//testing")) {
            if (System.getProperty(ExtMongo.TEST_USER_PROPERTY) == null) {
                client = new MongoClient(
                    ServerAddress.defaultHost(),
                    ExtMongo.FAKE.apply(this.id)
                );
            } else {
                client = realMongoClient(
                    testHost(),
                    testPort(),
                    testUser(),
                    testPassword(),
                    testDbName()
                );
            }
        } else {
            client = realMongoClient(
                props.get("//mongo/host"),
                Integer.parseInt(props.get("//mongo/port")),
                props.get("//mongo/user"),
                props.get("//mongo/password"),
                props.get("//mongo/dbname")
            );
        }
        return client;
    }

    /**
     * Instantiates a real Mongo client.
     * @param host A host name
     * @param port A port number
     * @param user A user name
     * @param password A password
     * @param database A database name
     * @return A MongoClient
     * @checkstyle ParameterNumberCheck (5 lines)
     */
    private static MongoClient realMongoClient(final String host,
        final int port,
        final String user,
        final String password,
        final String database) {
        return new MongoClient(
            new ServerAddress(
                host,
                port
            ),
            MongoCredential.createCredential(
                user,
                database,
                password.toCharArray()
            ),
            MongoClientOptions.builder()
                .maxWaitTime(ExtMongo.DEFAULT_TIMEOUT)
                .socketTimeout(ExtMongo.DEFAULT_TIMEOUT)
                .connectTimeout(ExtMongo.DEFAULT_TIMEOUT)
                .serverSelectionTimeout(ExtMongo.DEFAULT_TIMEOUT)
                .build()
        );
    }

    /**
     * Obtains the test Mongo host name.
     * @return The test Mongo host name
     */
    private static String testHost() {
        return System.getProperty(
            ExtMongo.TEST_HOST_PROPERTY,
            ServerAddress.defaultHost()
        );
    }

    /**
     * Obtains the test Mongo port number.
     * @return The test Mongo port
     */
    private static int testPort() {
        return Integer.getInteger(
            ExtMongo.TEST_PORT_PROPERTY,
            ServerAddress.defaultPort()
        );
    }

    /**
     * Obtains the test Mongo user.
     * @return The test Mongo user
     */
    private static String testUser() {
        return System.getProperty(ExtMongo.TEST_USER_PROPERTY);
    }

    /**
     * Obtains the test Mongo password.
     * @return The test Mongo password
     */
    private static String testPassword() {
        return System.getProperty(
            ExtMongo.TEST_PASSWORD_PROPERTY,
            ExtMongo.DEFAULT_PASSWORD
        );
    }

    /**
     * Obtains the test Mongo database name.
     * @return The test Mongo database name
     */
    private static String testDbName() {
        return System.getProperty(
            ExtMongo.TEST_DB_NAME_PROPERTY,
            ExtMongo.DEFAULT_DB_NAME
        );
    }

}
