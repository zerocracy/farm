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
package com.zerocracy.tk.profile;

import com.zerocracy.jstk.Project;
import com.zerocracy.pmo.Agenda;
import com.zerocracy.pmo.Awards;
import com.zerocracy.pmo.People;
import com.zerocracy.tk.RsPage;
import java.io.IOException;
import java.util.Properties;
import org.cactoos.list.LengthOfIterable;
import org.takes.Response;
import org.takes.facets.auth.RqAuth;
import org.takes.facets.fork.RqRegex;
import org.takes.facets.fork.TkRegex;
import org.takes.rs.xe.XeAppend;
import org.takes.rs.xe.XeTransform;
import org.takes.rs.xe.XeWhen;

/**
 * User profile page.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.12
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class TkProfile implements TkRegex {

    /**
     * Properties.
     */
    private final Properties props;

    /**
     * PMO.
     */
    private final Project pmo;

    /**
     * Ctor.
     * @param pps Properties
     * @param pkt Project
     */
    public TkProfile(final Properties pps, final Project pkt) {
        this.props = pps;
        this.pmo = pkt;
    }

    @Override
    public Response act(final RqRegex req) throws IOException {
        final String login = new RqLogin(this.pmo, req).value();
        final People people = new People(this.pmo).bootstrap();
        return new RsPage(
            this.props,
            "/xsl/profile.xsl",
            req,
            new XeWhen(
                login.equals(
                    new RqAuth(req).identity().properties().get("login")
                ),
                new XeAppend(
                    "details",
                    new XeAppend("rate", people.rate(login).toString()),
                    new XeAppend("wallet", people.wallet(login)),
                    new XeAppend("bank", people.bank(login)),
                    new XeAppend(
                        "skills",
                        new XeTransform<>(
                            people.skills(login),
                            skill -> new XeAppend("skill", skill)
                        )
                    ),
                    new XeAppend(
                        "links",
                        new XeTransform<>(
                            people.links(login),
                            link -> new XeAppend("link", link)
                        )
                    )
                )
            ),
            new XeAppend(
                "awards",
                Integer.toString(
                    new Awards(this.pmo, login).bootstrap().total()
                )
            ),
            new XeAppend(
                "agenda",
                Integer.toString(
                    new LengthOfIterable(
                        new Agenda(this.pmo, login).bootstrap().jobs()
                    ).value()
                )
            )
        );
    }

}
