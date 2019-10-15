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
import com.zerocracy.Xocument;
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
 * @checkstyle LineLengthCheck (500 line)
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
                try (final Item item = new Pmo(this.farm).acq("catalog.xml", Project.Mode.READ_ONLY)) {
                    new And(
                        new FuncOf<>(
                            input -> sources.add(
                                TkBoard.source(
                                    input, user
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
     * @param node Project XML
     * @param user Current user
     * @return Source
     */
    private static XeSource source(final XML node, final String user) {
        final Collection<String> members = node.xpath("members/member/text()");
        final String pid = node.xpath("@id").get(0);
        return new XeAppend(
            "project",
            new XeAppend(
                "sandbox",
                node.xpath("sandbox/text()").stream()
                    .findFirst().orElse(Boolean.toString(false))
            ),
            new XeAppend("id", pid),
            new XeAppend(
                "title",
                node.xpath("title/text()").stream()
                    .findFirst().orElse(pid)
            ),
            new XeAppend("mine", Boolean.toString(members.contains(user))),
            new XeAppend(
                "architects",
                new XeAppend(
                    "architect", node.xpath("architect/text()").get(0)
                )
            ),
            new XeAppend(
                "repositories",
                new XeTransform<>(
                    node.xpath("links/link[@rel='github']/@href"),
                    repo -> new XeAppend("repository", repo)
                )
            ),
            new XeAppend(
                "languages",
                String.join(", ", node.xpath("languages/text()"))
            ),
            new XeAppend("jobs", node.xpath("jobs/text()").get(0)),
            new XeAppend("orders", node.xpath("orders/text()").get(0)),
            new XeAppend("deficit", node.xpath("cash/@deficit").get(0)),
            new XeAppend("cash", node.xpath("cash/text()").get(0)),
            new XeAppend("members", Integer.toString(members.size()))
        );
    }
}
