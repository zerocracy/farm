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
package com.zerocracy.tk;

import com.zerocracy.Farm;
import com.zerocracy.Item;
import com.zerocracy.Xocument;
import com.zerocracy.pmo.Pmo;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import org.cactoos.func.FuncOf;
import org.cactoos.iterable.Filtered;
import org.cactoos.scalar.And;
import org.takes.Request;
import org.takes.Response;
import org.takes.Take;
import org.takes.rs.xe.XeAppend;
import org.takes.rs.xe.XeChain;
import org.takes.rs.xe.XeSource;
import org.takes.rs.xe.XeWhen;

/**
 * List of all people.
 *
 * @since 1.0
 * @todo #559:30min Let's display the data from people/skills on the profile
 *  page of a given user as well as on the team page.
 * @checkstyle ClassDataAbstractionCouplingCheck (3 lines)
 * @checkstyle LineLengthCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class TkTeam implements Take {

    /**
     * Farm.
     */
    private final Farm farm;

    /**
     * Ctor.
     * @param frm Farm
     */
    public TkTeam(final Farm frm) {
        this.farm = frm;
    }

    @Override
    public Response act(final Request req) throws IOException {
        return new RsPage(
            this.farm,
            "/xsl/team.xsl",
            req,
            () -> {
                new RqUser(this.farm, req, false).value();
                final Collection<XeSource> sources = new LinkedList<>();
                try (final Item item = new Pmo(this.farm).acq("people.xml")) {
                    new And(
                        new FuncOf<>(
                            node -> sources.add(
                                new XeAppend(
                                    "user",
                                    new XeChain(
                                        new XeAppend("login", node.xpath("@id").get(0)),
                                        new XeAppend("mentor", node.xpath("mentor/text()").get(0)),
                                        new XeAppend("awards", node.xpath("reputation/text()").get(0)),
                                        new XeAppend("speed", node.xpath("speed/text()").get(0)),
                                        new XeAppend("agenda", node.xpath("jobs/text()").get(0)),
                                        new XeAppend("projects", node.xpath("projects/text()").get(0)),
                                        new XeWhen(
                                            !node.nodes("vacation").isEmpty()
                                                && "true".equals(node.xpath("vacation/text()").get(0)),
                                            new XeAppend("vacation", "true")
                                        ),
                                        new XeWhen(
                                            !node.nodes("rate").isEmpty(),
                                            () -> new XeAppend("rate", node.xpath("rate/text()").get(0))
                                        )
                                    )
                                )
                            ),
                            true
                        ),
                        new Filtered<>(
                            node -> Boolean.parseBoolean(node.xpath("active/text()").get(0)),
                            new Xocument(item).nodes("/people/person[mentor]")
                        )
                    ).value();
                }
                return new XeAppend("people", new XeChain(sources));
            }
        );
    }
}
