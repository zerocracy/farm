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
package com.zerocracy.tk;

import com.jcabi.xml.XML;
import com.zerocracy.Xocument;
import com.zerocracy.jstk.Farm;
import com.zerocracy.jstk.Item;
import com.zerocracy.jstk.Project;
import com.zerocracy.pm.scope.Wbs;
import com.zerocracy.pm.staff.Roles;
import com.zerocracy.pmo.Pmo;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Properties;
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
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.13
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class TkBoard implements Take {

    /**
     * Properties.
     */
    private final Properties props;

    /**
     * Farm.
     */
    private final Farm farm;

    /**
     * Ctor.
     * @param pps Properties
     * @param frm Farm
     */
    public TkBoard(final Properties pps, final Farm frm) {
        this.props = pps;
        this.farm = frm;
    }

    @Override
    public Response act(final Request req) throws IOException {
        return new RsPage(
            this.props,
            "/xsl/board.xsl",
            req,
            () -> {
                final String user = new RqUser(this.farm, req).value();
                final Collection<XeSource> sources = new LinkedList<>();
                try (final Item item = new Pmo(this.farm).acq("catalog.xml")) {
                    new And(
                        new Xocument(item).nodes(
                            "/catalog/project[@id!='PMO']"
                        ),
                        new FuncOf<>(
                            input -> sources.add(this.source(input, user)),
                            true
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
        return new XeAppend(
            "project",
            new XeAppend("id", project.toString()),
            new XeAppend(
                "mine",
                Boolean.toString(
                    new Roles(project).bootstrap().hasAnyRole(user)
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
                "jobs",
                Integer.toString(new Wbs(project).bootstrap().iterate().size())
            ),
            new XeAppend(
                "members",
                Integer.toString(
                    new Roles(project).bootstrap().everybody().size()
                )
            )
        );
    }

}
