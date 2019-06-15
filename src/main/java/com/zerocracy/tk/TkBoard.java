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
import com.zerocracy.Project;
import com.zerocracy.Txn;
import com.zerocracy.Xocument;
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
                // @checkstyle LineLengthCheck (1 line)
                try (final Txn pmo = new Txn(new Pmo(this.farm)); final Item item = pmo.acq("catalog.xml")) {
                    new Catalog(pmo).bootstrap();
                    new And(
                        new FuncOf<>(
                            input -> sources.add(
                                TkBoard.source(
                                    pmo, input.xpath("@id").get(0), user
                                )
                            ),
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
     * @param pmo PMO transaction
     * @param pid Project id
     * @param user Current user
     * @return Source
     * @throws IOException If fails
     */
    private static XeSource source(final Project pmo, final String pid,
        final String user) throws IOException {
        final Catalog catalog = new Catalog(pmo);
        final Collection<String> members = catalog.members(pid);
        return new XeAppend(
            "project",
            new XeAppend(
                "sandbox",
                Boolean.toString(catalog.sandbox(pid))
            ),
            new XeAppend("id", pid),
            new XeAppend("title", catalog.title(pid)),
            new XeAppend("mine", Boolean.toString(members.contains(user))),
            new XeAppend(
                "architects",
                new XeAppend(
                    "architect",
                    catalog.architect(pid)
                )
            ),
            new XeAppend(
                "repositories",
                new XeTransform<>(
                    catalog.links(pid, "github"),
                    repo -> new XeAppend("repository", repo)
                )
            ),
            new XeAppend(
                "languages", String.join(", ", catalog.languages(pid))
            ),
            new XeAppend("jobs", Integer.toString(catalog.jobs(pid))),
            new XeAppend("orders", Integer.toString(catalog.orders(pid))),
            new XeAppend("deficit", Boolean.toString(catalog.deficit(pid))),
            new XeAppend("cash", catalog.cash(pid).toString()),
            new XeAppend("members", Integer.toString(members.size()))
        );
    }
}
