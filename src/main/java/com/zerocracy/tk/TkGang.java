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
package com.zerocracy.tk;

import com.jcabi.xml.XML;
import com.zerocracy.Farm;
import com.zerocracy.Item;
import com.zerocracy.Xocument;
import com.zerocracy.pmo.Agenda;
import com.zerocracy.pmo.Pmo;
import com.zerocracy.pmo.Projects;
import com.zerocracy.pmo.Speed;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import org.cactoos.func.FuncOf;
import org.cactoos.iterable.LengthOf;
import org.cactoos.scalar.And;
import org.takes.Request;
import org.takes.Response;
import org.takes.Take;
import org.takes.rs.xe.XeAppend;
import org.takes.rs.xe.XeChain;
import org.takes.rs.xe.XeSource;
import org.takes.rs.xe.XeWhen;

/**
 * Gang of all people.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.19
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class TkGang implements Take {

    /**
     * Farm.
     */
    private final Farm farm;

    /**
     * Ctor.
     * @param frm Farm
     */
    public TkGang(final Farm frm) {
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
                            input -> sources.add(this.source(input)),
                            true
                        ),
                        new Xocument(item).nodes("/people/person[mentor]")
                    ).value();
                }
                return new XeAppend("people", new XeChain(sources));
            }
        );
    }

    /**
     * Create source for one user.
     * @param node XML node
     * @return Source
     * @throws IOException If fails
     */
    private XeSource source(final XML node) throws IOException {
        final String login = node.xpath("@id").get(0);
        return new XeAppend(
            "user",
            new XeChain(
                new XeAppend("login", node.xpath("@id").get(0)),
                new XeAppend("mentor", node.xpath("mentor/text()").get(0)),
                new XeAppend("awards", node.xpath("reputation/text()").get(0)),
                new XeAppend(
                    "speed",
                    Double.toString(
                        new Speed(this.farm, login).bootstrap().avg()
                    )
                ),
                new XeAppend(
                    "agenda",
                    Integer.toString(
                        new LengthOf(
                            new Agenda(this.farm, login).bootstrap().jobs()
                        ).intValue()
                    )
                ),
                new XeAppend(
                    "projects",
                    Integer.toString(
                        new LengthOf(
                            new Projects(this.farm, login).bootstrap()
                                .iterate()
                        ).intValue()
                    )
                ),
                new XeWhen(
                    !node.nodes("vacation").isEmpty()
                        && "true".equals(node.xpath("vacation/text()")),
                    new XeAppend("vacation", "true")
                ),
                new XeWhen(
                    !node.nodes("rate").isEmpty(),
                    () -> new XeAppend("rate", node.xpath("rate/text()").get(0))
                )
            )
        );
    }
}
