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
package com.zerocracy.tk.profile;

import com.zerocracy.Farm;
import com.zerocracy.pmo.Agenda;
import com.zerocracy.pmo.Awards;
import com.zerocracy.pmo.Catalog;
import com.zerocracy.pmo.Debts;
import com.zerocracy.pmo.People;
import com.zerocracy.pmo.Pmo;
import com.zerocracy.pmo.Projects;
import com.zerocracy.tk.RqUser;
import com.zerocracy.tk.RsPage;
import java.io.IOException;
import org.cactoos.iterable.LengthOf;
import org.takes.Response;
import org.takes.facets.fork.RqRegex;
import org.takes.facets.fork.TkRegex;
import org.takes.rs.xe.XeAppend;
import org.takes.rs.xe.XeChain;
import org.takes.rs.xe.XeDirectives;
import org.takes.rs.xe.XeTransform;
import org.takes.rs.xe.XeWhen;
import org.xembly.Directives;

/**
 * User profile page.
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class TkProfile implements TkRegex {

    /**
     * Farm.
     */
    private final Farm farm;

    /**
     * Ctor.
     * @param frm Farm
     */
    public TkProfile(final Farm frm) {
        this.farm = frm;
    }

    @Override
    public Response act(final RqRegex req) throws IOException {
        final Pmo pmo = new Pmo(this.farm);
        return new RsPage(
            this.farm,
            "/xsl/profile.xsl",
            req,
            () -> {
                final String user = new RqUser(this.farm, req).value();
                final String login = new RqLogin(this.farm, req).value();
                final People people = new People(pmo).bootstrap();
                final Catalog catalog = new Catalog(pmo).bootstrap();
                final Debts debts = new Debts(pmo).bootstrap();
                return new XeChain(
                    new XeAppend("owner", login),
                    new XeWhen(
                        login.equals(user),
                        new XeAppend(
                            "details",
                            new XeAppend(
                                "wallet",
                                new XeAppend("info", people.wallet(login)),
                                new XeAppend("bank", people.bank(login))
                            ),
                            new XeAppend(
                                "modifies_vacation_mode",
                                Boolean.toString(people.vacation(login))
                            ),
                            new XeAppend(
                                "projects",
                                new XeTransform<>(
                                    new Projects(pmo, login).bootstrap()
                                        .iterate(),
                                    pkt -> new XeDirectives(
                                        new Directives()
                                            .add("project")
                                            .attr("title", catalog.title(pkt))
                                            .set(pkt)
                                    )
                                )
                            ),
                            new XeAppend(
                                "links",
                                new XeTransform<>(
                                    people.links(login),
                                    link -> new XeAppend("link", link)
                                )
                            ),
                            new XeAppend(
                                "identification",
                                people.details(login)
                            ),
                            new XeWhen(
                                debts.exists(login),
                                new XeDirectives(debts.toXembly(login))
                            )
                        )
                    ),
                    new XeAppend(
                        "identified",
                        Boolean.toString(!people.details(login).isEmpty())
                    ),
                    new XeAppend("rate", people.rate(login).toString()),
                    new XeAppend(
                        "awards",
                        Integer.toString(
                            new Awards(pmo, login).bootstrap().total()
                        )
                    ),
                    new XeAppend(
                        "agenda",
                        Integer.toString(
                            new LengthOf(
                                new Agenda(pmo, login).bootstrap().jobs()
                            ).intValue()
                        )
                    ),
                    new XeWhen(
                        people.hasMentor(login),
                        new XeAppend(
                            "mentor",
                            () -> new XeDirectives(
                                new Directives().set(people.mentor(login))
                            )
                        )
                    ),
                    new XeAppend(
                        "students",
                        new XeTransform<>(
                            people.students(login),
                            st -> new XeAppend("student", st)
                        )
                    )
                );
            }
        );
    }

}
