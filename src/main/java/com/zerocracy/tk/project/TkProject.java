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
package com.zerocracy.tk.project;

import com.zerocracy.Farm;
import com.zerocracy.Project;
import com.zerocracy.farm.props.Props;
import com.zerocracy.pm.cost.Equity;
import com.zerocracy.pm.cost.Estimates;
import com.zerocracy.pm.cost.Ledger;
import com.zerocracy.pm.cost.Rates;
import com.zerocracy.pm.cost.Vesting;
import com.zerocracy.pm.staff.Roles;
import com.zerocracy.pmo.Catalog;
import com.zerocracy.pmo.Pmo;
import com.zerocracy.pmo.recharge.Recharge;
import com.zerocracy.tk.RqUser;
import com.zerocracy.tk.RsPage;
import java.io.IOException;
import org.takes.Response;
import org.takes.facets.fork.RqRegex;
import org.takes.facets.fork.TkRegex;
import org.takes.rs.xe.XeAppend;
import org.takes.rs.xe.XeChain;
import org.takes.rs.xe.XeTransform;
import org.takes.rs.xe.XeWhen;

/**
 * Project page.
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class TkProject implements TkRegex {

    /**
     * Farm.
     */
    private final Farm farm;

    /**
     * Ctor.
     * @param frm Farm
     */
    public TkProject(final Farm frm) {
        this.farm = frm;
    }

    @Override
    @SuppressWarnings("PMD.ExcessiveMethodLength")
    public Response act(final RqRegex req) throws IOException {
        return new RsPage(
            this.farm,
            "/xsl/project.xsl",
            req,
            () -> {
                final Project project = new RqProject(this.farm, req);
                final Catalog catalog = new Catalog(this.farm).bootstrap();
                final String pid = project.pid();
                final Recharge recharge = new Recharge(this.farm, project);
                final String user = new RqUser(this.farm, req, false).value();
                return new XeChain(
                    new XeAppend("project", pid),
                    new XeAppend("title", catalog.title(pid)),
                    new XeWhen(
                        !"PMO".equals(pid),
                        () -> {
                            final Roles roles = new Roles(project).bootstrap();
                            final Rates rates = new Rates(project).bootstrap();
                            final Vesting vesting =
                                new Vesting(project).bootstrap();
                            return new XeChain(
                                new XeAppend(
                                    "pause",
                                    Boolean.toString(catalog.pause(pid))
                                ),
                                new XeAppend(
                                    "published",
                                    Boolean.toString(catalog.published(pid))
                                ),
                                new XeWhen(
                                    catalog.published(pid),
                                    new XeChain(
                                        new XeAppend(
                                            "architects",
                                            new XeTransform<String>(
                                                roles.findByRole("ARC"),
                                                usr -> new XeAppend(
                                                    "architect", usr
                                                )
                                            )
                                        )
                                    )
                                ),
                                new XeWhen(
                                    rates.exists(user),
                                    () -> new XeAppend(
                                        "rate",
                                        rates.rate(user).toString()
                                    )
                                ),
                                new XeWhen(
                                    vesting.exists(user),
                                    () -> new XeAppend(
                                        "vesting",
                                        vesting.rate(user).toString()
                                    )
                                ),
                                new XeAppend(
                                    "ownership",
                                    new Equity(project).bootstrap()
                                        .ownership(user)
                                ),
                                new XeWhen(
                                    recharge.exists(),
                                    () -> new XeAppend(
                                        "recharge",
                                        recharge.amount().toString()
                                    )
                                ),
                                new XeAppend(
                                    "roles",
                                    new XeTransform<String>(
                                        roles.allRoles(user),
                                        role -> new XeAppend("role", role)
                                    )
                                ),
                                new XeWhen(
                                    new Roles(
                                        new Pmo(this.farm)
                                    ).bootstrap().hasAnyRole(user)
                                    || roles.hasRole(user, "PO"),
                                    new XeChain(
                                        new XeAppend(
                                            "stripe_key",
                                            new Props(this.farm).get(
                                                "//stripe/key", ""
                                            )
                                        ),
                                        new XeAppend(
                                            "cash",
                                            new Ledger(this.farm, project)
                                                .bootstrap().cash().toString()
                                        ),
                                        new XeAppend(
                                            "estimates",
                                            new Estimates(this.farm, project)
                                                .bootstrap().total().toString()
                                        ),
                                        new XeAppend(
                                            "deficit",
                                            Boolean.toString(
                                                new Ledger(this.farm, project)
                                                    .bootstrap().deficit()
                                            )
                                        ),
                                        new XeAppend(
                                            "fee",
                                            catalog.fee(
                                                project.pid()
                                            ).toString()
                                        )
                                    )
                                )
                            );
                        }
                    ),
                    new XeAppend(
                        "project_links",
                        new XeTransform<>(
                            new Catalog(new Pmo(this.farm)).links(pid),
                            link -> new XeAppend("link", link)
                        )
                    )
                );
            }
        );
    }

}
