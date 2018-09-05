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
package com.zerocracy.tk;

import com.jcabi.github.Coordinates;
import com.jcabi.github.Github;
import com.jcabi.github.Language;
import com.jcabi.xml.XML;
import com.zerocracy.Farm;
import com.zerocracy.Item;
import com.zerocracy.Project;
import com.zerocracy.Xocument;
import com.zerocracy.entry.ExtGithub;
import com.zerocracy.pm.cost.Estimates;
import com.zerocracy.pm.cost.Ledger;
import com.zerocracy.pm.in.Orders;
import com.zerocracy.pm.scope.Wbs;
import com.zerocracy.pm.staff.Roles;
import com.zerocracy.pmo.Catalog;
import com.zerocracy.pmo.Pmo;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import org.cactoos.func.FuncOf;
import org.cactoos.scalar.And;
import org.cactoos.text.JoinedText;
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
     * Github.
     */
    private final Github github;

    /**
     * Ctor.
     * @param frm Farm
     */
    public TkBoard(final Farm frm) {
        this(frm, new ExtGithub(frm).value());
    }

    /**
     * Ctor.
     * @param frm Farm
     * @param github Github
     */
    TkBoard(final Farm frm, final Github github) {
        this.farm = frm;
        this.github = github;
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
     */
    private XeSource source(final XML node, final String user)
        throws IOException {
        final Project project = this.farm.find(
            String.format("@id='%s'", node.xpath("@id").get(0))
        ).iterator().next();
        final Catalog catalog = new Catalog(this.farm).bootstrap();
        final Roles roles = new Roles(project).bootstrap();
        final Ledger ledger = new Ledger(project).bootstrap();
        return new XeAppend(
            "project",
            new XeAppend(
                "sandbox",
                Boolean.toString(catalog.sandbox().contains(project.pid()))
            ),
            new XeAppend("id", project.pid()),
            new XeAppend("title", catalog.title(project.pid())),
            new XeAppend("mine", Boolean.toString(roles.hasAnyRole(user))),
            new XeAppend(
                "architects",
                new XeTransform<>(
                    roles.findByRole("ARC"),
                    login -> new XeAppend("architect", login)
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
                "languages", this.languages(node)
            ),
            new XeAppend(
                "jobs",
                Integer.toString(new Wbs(project).bootstrap().iterate().size())
            ),
            new XeAppend(
                "orders",
                Integer.toString(
                    new Orders(project).bootstrap().iterate().size()
                )
            ),
            new XeAppend(
                "deficit",
                Boolean.toString(ledger.deficit())
            ),
            new XeAppend(
                "cash",
                ledger.cash().add(
                    new Estimates(project).bootstrap().total().mul(-1L)
                ).toString()
            ),
            new XeAppend(
                "members",
                Integer.toString(
                    new Roles(project).bootstrap().everybody().size()
                )
            )
        );
    }

    /**
     * Get languages from repos.
     * @param node XML
     * @return Languages
     * @throws IOException If an IO error occurs
     * @todo #930:30min Right now we are displaying all languages for all
     *  repositories. We should only display the top 4 languages (ranked by
     *  bytes of code, as returned by Github) across all project repos.
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private String languages(final XML node) throws IOException {
        final Set<String> langs = new HashSet<>();
        for (final String repo
            : node.xpath("links/link[@rel='github']/@href")) {
            for (final Language lang : this.github.repos()
                .get(new Coordinates.Simple(repo)).languages()) {
                langs.add(lang.name());
            }
        }
        return new JoinedText(",", langs).asString();
    }

}
