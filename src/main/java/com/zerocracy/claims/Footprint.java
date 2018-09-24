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
package com.zerocracy.claims;

import com.jcabi.aspects.Tv;
import com.jcabi.xml.XML;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.zerocracy.Farm;
import com.zerocracy.Project;
import com.zerocracy.entry.ExtMongo;
import com.zerocracy.farm.props.Props;
import java.io.Closeable;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.bson.Document;

/**
 * Footprint.
 *
 * @since 1.0
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class Footprint implements Closeable {

    /**
     * Footprint lock.
     */
    private static final Object LOCK = new Object();

    /**
     * Database name.
     */
    private static final String DBNAME = "footprint";
    /**
     * Collection.
     */
    private static final String CLAIMS = "claims";
    /**
     * Project ID.
     */
    private final String pid;

    /**
     * Mongo client.
     */
    private final MongoClient mongo;

    /**
     * Ctor.
     * @param farm Farm
     * @param pkt Project
     * @throws IOException If fails
     */
    public Footprint(final Farm farm, final Project pkt) throws IOException {
        this(new ExtMongo(farm).value(), pkt.pid());
    }

    /**
     * Ctor.
     * @param clt Client
     * @param pkt Project name
     */
    public Footprint(final MongoClient clt, final String pkt) {
        this.mongo = clt;
        this.pid = pkt;
    }

    /**
     * Add new claim, it was just opened.
     * @param xml The claim XML
     * @param signature Claim signature
     * @throws IOException If fails
     */
    public void open(final XML xml, final String signature)
        throws IOException {
        synchronized (Footprint.LOCK) {
            final ClaimIn claim = new ClaimIn(xml);
            final String cid = claim.cid();
            final MongoCollection<Document> col =
                this.mongo.getDatabase("footprint")
                    .getCollection("claims");
            final Iterator<Document> found = col.find(
                Filters.and(
                    Filters.eq("cid", cid),
                    Filters.eq("project", this.pid)
                )
            ).iterator();
            if (found.hasNext()) {
                throw new IllegalArgumentException(
                    String.format(
                        "Claim #%s (%s) already exists for %s",
                        cid, claim.type(), this.pid
                    )
                );
            }
            Document doc = new Document()
                .append("cid", cid)
                .append("version", new Props().get("//build/version", ""))
                .append("project", this.pid)
                .append("type", claim.type())
                .append("created", claim.created())
                .append("signature", signature);
            if (claim.hasAuthor()) {
                doc = doc.append("author", claim.author());
            }
            if (claim.hasToken()) {
                doc = doc.append("token", claim.token());
            }
            final Set<Map.Entry<String, String>> entries =
                claim.params().entrySet();
            for (final Map.Entry<String, String> ent : entries) {
                final Object val;
                if (ent.getValue().matches("[0-9]+")) {
                    val = Long.parseLong(ent.getValue());
                } else {
                    val = ent.getValue();
                }
                doc = doc.append(ent.getKey(), val);
            }
            col.insertOne(doc);
        }
    }

    /**
     * Close this claim.
     * @param xml The claim XML
     */
    public void close(final XML xml) {
        synchronized (Footprint.LOCK) {
            final ClaimIn claim = new ClaimIn(xml);
            this.mongo.getDatabase(Footprint.DBNAME)
                .getCollection(Footprint.CLAIMS)
                .updateOne(
                    Filters.and(
                        Filters.eq("cid", claim.cid()),
                        Filters.eq("project", this.pid),
                        Filters.eq("type", claim.type()),
                        Filters.eq("created", claim.created())
                    ),
                    Updates.currentDate("closed")
                );
        }
    }

    /**
     * Remove some claims which are older than 30 days.
     * @param now Time now
     * @return Deleted claims
     */
    public long cleanup(final Date now) {
        return this.mongo.getDatabase(Footprint.DBNAME)
            .getCollection(Footprint.CLAIMS)
            .deleteMany(
                Filters.and(
                    Filters.regex("type", "Notify.*"),
                    Filters.lt(
                        "created",
                        new Date(
                            now.getTime() - TimeUnit.DAYS.toMillis(
                                (long) Tv.THIRTY
                            )
                        )
                    )
                )
            ).getDeletedCount();
    }

    /**
     * Mongo collection to work with.
     * @return Collection
     */
    public MongoCollection<Document> collection() {
        return this.mongo.getDatabase(Footprint.DBNAME)
            .getCollection(Footprint.CLAIMS);
    }

    @Override
    public void close() {
        this.mongo.close();
    }
}
