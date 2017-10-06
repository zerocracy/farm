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

import com.jcabi.xml.XML;
import com.jcabi.xml.XMLDocument;
import com.mongodb.MongoClient;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.zerocracy.jstk.Item;
import com.zerocracy.pm.ClaimIn;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import lombok.EqualsAndHashCode;
import org.bson.Document;
import org.cactoos.io.LengthOf;
import org.cactoos.io.TeeInput;

/**
 * Footprint item.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.18
 */
@EqualsAndHashCode(of = "origin")
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
final class FtItem implements Item {

    /**
     * Project ID.
     */
    private final String pid;

    /**
     * Original item.
     */
    private final Item origin;

    /**
     * Mongo.
     */
    private final MongoClient mongo;

    /**
     * Path to content temp.
     */
    private final Path temp;

    /**
     * Ctor.
     * @param pkt Project ID
     * @param item Original item
     * @param client Mongo client
     * @param path Content temp
     * @checkstyle ParameterNumberCheck (5 lines)
     */
    FtItem(final String pkt, final Item item,
        final MongoClient client, final Path path) {
        this.pid = pkt;
        this.origin = item;
        this.mongo = client;
        this.temp = path;
    }

    @Override
    public String toString() {
        return this.origin.toString();
    }

    @Override
    public Path path() throws IOException {
        return this.origin.path();
    }

    @Override
    public void close() throws IOException {
        final Path modified = Files.createTempFile("footprint", ".xml");
        new LengthOf(new TeeInput(this.path(), modified)).value();
        final XML after = FtItem.claims(modified);
        this.origin.close();
        final XML before = FtItem.claims(this.temp);
        for (final XML claim : before.nodes("//claim")) {
            if (!FtItem.exists(after, claim)) {
                this.closed(claim);
            }
        }
        for (final XML claim : after.nodes("//claim")) {
            if (!FtItem.exists(before, claim)) {
                this.opened(claim);
            }
        }
        Files.delete(this.temp);
        Files.delete(modified);
    }

    /**
     * The claim exists?
     * @param xml Document
     * @param claim Claim to search for
     * @return TRUE if it exists
     */
    private static boolean exists(final XML xml, final XML claim) {
        return !xml.nodes(
            String.format(
                "//claim[@id='%s']", claim.xpath("@id").get(0)
            )
        ).isEmpty();
    }

    /**
     * Load claims as XML document.
     * @param path The path
     * @return XML document
     * @throws IOException If fails
     */
    private static XML claims(final Path path) throws IOException {
        final XML xml;
        if (path.toFile().length() > 0L) {
            xml = new XMLDocument(path.toFile());
        } else {
            xml = new XMLDocument("<claims/>");
        }
        return xml;
    }

    /**
     * The claim was closed.
     * @param xml The claim
     */
    private void closed(final XML xml) {
        final ClaimIn claim = new ClaimIn(xml);
        this.mongo.getDatabase("footprint").getCollection("claims").updateOne(
            Filters.and(
                Filters.eq("cid", claim.cid()),
                Filters.eq("project", this.pid),
                Filters.eq("type", claim.type()),
                Filters.eq("created", claim.created())
            ),
            Updates.currentDate("closed")
        );
    }

    /**
     * The claim was opened.
     * @param xml The claim
     */
    private void opened(final XML xml) {
        final ClaimIn claim = new ClaimIn(xml);
        Document doc = new Document()
            .append("cid", claim.cid())
            .append("project", this.pid)
            .append("type", claim.type())
            .append("created", claim.created());
        if (claim.hasAuthor()) {
            doc = doc.append("author", claim.author());
        }
        if (claim.hasToken()) {
            doc = doc.append("token", claim.token());
        }
        for (final Map.Entry<String, String> ent : claim.params().entrySet()) {
            doc = doc.append(ent.getKey(), ent.getValue());
        }
        this.mongo.getDatabase("footprint")
            .getCollection("claims")
            .insertOne(doc);
    }

}
