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

import com.jcabi.xml.XML;
import com.zerocracy.Farm;
import com.zerocracy.Item;
import com.zerocracy.Project;
import com.zerocracy.Txn;
import com.zerocracy.Xocument;
import com.zerocracy.pm.staff.Roles;
import com.zerocracy.pmo.Catalog;
import com.zerocracy.pmo.Pmo;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import org.cactoos.func.FuncOf;
import org.cactoos.scalar.And;
import org.takes.Request;
import org.takes.Response;
import org.takes.Take;
import org.takes.rs.xe.XeAppend;
import org.takes.rs.xe.XeChain;
import org.takes.rs.xe.XeSource;
import org.takes.rs.xe.XeTransform;

/**
 * Board of projects.
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@SuppressWarnings({"PMD.ExcessiveImports", "PMD.AvoidDuplicateLiterals"})
public final class TkBoard implements Take {

    /**
     * Farm.
     */
    private final Farm farm;

    /**
     * Ctor.
     * @param frm Farm
     */
    public TkBoard(final Farm frm) {
        this.farm = frm;
    }

    @Override
    public Response act(final Request req) throws IOException {
        return new RsPage(
            this.farm,
            "/xsl/board.xsl",
            req,
            () -> {
                final String user = new RqUser(this.farm, req, false).value();
                final Collection<XeSource> sources = new LinkedList<>();
                try (final Item item = new Pmo(this.farm).acq("catalog.xml")) {
                    new And(
                        new FuncOf<>(
                            input -> sources.add(this.source(input, user)),
                            true
                        ),
                        new Xocument(item).nodes(
                            "/catalog/project[@id!='PMO' and publish='true']"
                        )
                    ).value();
                }
                return new XeAppend("projects", new XeChain(sources));
            }
        );
    }

    /**
     * Create source for one project.
     * @param node XML node
     * @param user Current user
     * @return Source
     * @throws IOException If fails
     * @checkstyle LineLengthCheck (100 lines)
     */
    private XeSource source(final XML node, final String user)
        throws IOException {
        final Project project = this.farm.find(
            String.format("@id='%s'", node.xpath("@id").get(0))
        ).iterator().next();
        try (final Txn txn = new Txn(new Pmo(this.farm))) {
            final Catalog catalog = new Catalog(txn).bootstrap();
            final Roles roles = new Roles(project).bootstrap();
            final String pid = project.pid();
            return new XeAppend(
                "project",
                new XeAppend(
                    "sandbox", Boolean.toString(catalog.sandbox().contains(pid))
                ),
                new XeAppend("id", pid),
                new XeAppend("title", catalog.title(pid)),
                new XeAppend("mine", Boolean.toString(roles.hasAnyRole(user))),
                new XeAppend("architects", catalog.architect(pid)),
                new XeAppend(
                    "repositories",
                    new XeTransform<>(
                        node.xpath("links/link[@rel='github']/@href"),
                        repo -> new XeAppend("repository", repo)
                    )
                ),
                new XeAppend(
                    "languages", String.join(",", catalog.languages(pid))
                ),
                new XeAppend("jobs", Integer.toString(catalog.jobs(pid))),
                new XeAppend("orders", Integer.toString(catalog.orders(pid))),
                new XeAppend("deficit", Boolean.toString(catalog.deficit(pid))),
                new XeAppend("cash", catalog.cash(pid).toString()),
                new XeAppend(
                    "members", Integer.toString(catalog.members(pid).size())
                )
            );
        }
    }
}
