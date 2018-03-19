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

import com.jcabi.xml.XML;
import com.mongodb.MongoClient;
import com.mongodb.client.model.Filters;
import com.zerocracy.Farm;
import com.zerocracy.Item;
import com.zerocracy.Project;
import com.zerocracy.RunsInThreads;
import com.zerocracy.farm.fake.FkFarm;
import com.zerocracy.farm.fake.FkProject;
import com.zerocracy.farm.props.PropsFarm;
import com.zerocracy.farm.sync.SyncFarm;
import com.zerocracy.pm.ClaimOut;
import com.zerocracy.pm.Claims;
import com.zerocracy.pm.Footprint;
import com.zerocracy.pmo.Pmo;
import java.util.concurrent.atomic.AtomicInteger;
import org.bson.BsonDocument;
import org.bson.BsonString;
import org.cactoos.io.LengthOf;
import org.cactoos.io.TeeInput;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Ignore;
import org.junit.Test;
import org.xembly.Directives;
import org.xembly.Xembler;

/**
 * Test case for {@link ExtMongo}.
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.18
 * @checkstyle JavadocMethodCheck (500 lines)
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class ExtMongoTest {

    @Test
    @Ignore
    public void connectsToProduction() throws Exception {
        final Project project = new FkProject();
        try (final Item item = project.acq("_props.xml")) {
            new LengthOf(
                new TeeInput(
                    new Xembler(
                        new Directives().add("props").add("mongo")
                            .add("host").set("ds253918.mlab.com").up()
                            .add("port").set("53918").up()
                            .add("user").set("admin").up()
                            .add("password").set("---").up()
                            .add("dbname").set("footprint").up()
                    ).xmlQuietly(),
                    item.path()
                )
            ).value();
        }
        final MongoClient client = new ExtMongo(new FkFarm(project)).value();
        MatcherAssert.assertThat(
            client.getDatabase("footprint").runCommand(
                new BsonDocument("buildinfo", new BsonString(""))
            ).get("version").toString(),
            Matchers.equalTo("3.5")
        );
    }

    @Test
    public void createsMongo() throws Exception {
        final Farm farm = new PropsFarm(new FkFarm());
        final Project project = new Pmo(farm);
        new ClaimOut().type("Hello").postTo(project);
        final XML xml = new Claims(project).iterate().iterator().next();
        final MongoClient mongo = new ExtMongo(farm).value();
        final String pid = "12MONGO89";
        try (final Footprint footprint = new Footprint(mongo, pid)) {
            footprint.open(xml);
            footprint.close(xml);
            MatcherAssert.assertThat(
                footprint.collection().find(Filters.eq("project", pid)),
                Matchers.iterableWithSize(1)
            );
        }
    }

    @Test
    public void worksInMultipleThreads() throws Exception {
        try (final Farm farm = new SyncFarm(new PropsFarm(new FkFarm()))) {
            final Project project = new Pmo(farm);
            final String pid = "123456799";
            MatcherAssert.assertThat(
                inc -> {
                    final MongoClient mongo = new ExtMongo(farm).value();
                    try (final Footprint footprint =
                        new Footprint(mongo, pid)) {
                        new ClaimOut()
                            .type("Version")
                            .param("something", inc.incrementAndGet())
                            .postTo(project);
                        final XML xml = new Claims(project)
                            .iterate().iterator().next();
                        footprint.open(xml);
                        footprint.close(xml);
                        return footprint.collection()
                            .find(Filters.eq("project", pid))
                            .iterator().hasNext();
                    }
                },
                new RunsInThreads<>(new AtomicInteger())
            );
        }
    }
}
