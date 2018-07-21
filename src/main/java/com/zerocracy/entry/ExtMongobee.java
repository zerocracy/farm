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

import com.github.mongobee.Mongobee;
import com.github.mongobee.exception.MongobeeException;
import com.github.zafarkhaja.semver.Version;
import com.jcabi.log.Logger;
import com.mongodb.MongoClient;
import com.zerocracy.Farm;
import com.zerocracy.farm.props.Props;
import java.io.IOException;
import org.bson.BsonDocument;
import org.bson.BsonString;

/**
 * Apply Mongobee changes.
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class ExtMongobee {

    /**
     * The farm.
     */
    private final Farm farm;

    /**
     * Ctor.
     * @param frm The farm
     */
    public ExtMongobee(final Farm frm) {
        this.farm = frm;
    }

    /**
     * Apply mongobee.
     * @throws IOException If fails
     */
    public void apply() throws IOException {
        final Props props = new Props(this.farm);
        try (final MongoClient client = new ExtMongo(this.farm).value()) {
            final String dbname = props.get("//mongo/dbname", "footprint");
            final Version version = Version.valueOf(
                client.getDatabase(dbname).runCommand(
                    new BsonDocument("buildinfo", new BsonString(""))
                ).get("version").toString()
            );
            if (version.compareTo(Version.valueOf("3.4.0")) < 0) {
                throw new IllegalStateException(
                    String.format(
                        "MongoDB server version is too old: %s",
                        version
                    )
                );
            }
            final Mongobee bee = new Mongobee(client);
            bee.setDbName(dbname);
            bee.setChangeLogsScanPackage(ExtMongo.class.getPackage().getName());
            bee.execute();
        } catch (final MongobeeException ex) {
            throw new IOException(ex);
        }
        Logger.info(this, "MongoDB updated");
    }

}
