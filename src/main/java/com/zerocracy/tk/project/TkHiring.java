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
import com.zerocracy.Par;
import com.zerocracy.Policy;
import com.zerocracy.Project;
import com.zerocracy.claims.ClaimOut;
import com.zerocracy.entry.ClaimsOf;
import com.zerocracy.pmo.Exam;
import com.zerocracy.pmo.Vacancies;
import com.zerocracy.tk.RqUser;
import com.zerocracy.tk.RsParFlash;
import java.io.IOException;
import java.util.logging.Level;
import org.takes.Response;
import org.takes.facets.fork.RqRegex;
import org.takes.facets.fork.TkRegex;
import org.takes.facets.forward.RsForward;
import org.takes.rq.RqGreedy;
import org.takes.rq.form.RqFormSmart;

/**
 * Announce hiring.
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class TkHiring implements TkRegex {

    /**
     * Farm.
     */
    private final Farm farm;

    /**
     * Ctor.
     * @param frm Farm
     */
    public TkHiring(final Farm frm) {
        this.farm = frm;
    }

    @Override
    public Response act(final RqRegex req) throws IOException {
        final Project project = new RqProject(this.farm, req, "PO", "ARC");
        final String user = new RqUser(this.farm, req, false).value();
        new Exam(this.farm, user).min("51.min", 0);
        final RqFormSmart form = new RqFormSmart(new RqGreedy(req));
        final String text = form.single("text");
        new ClaimOut()
            .type("Make payment")
            .author(user)
            .param("login", user)
            .param("job", "none")
            .param("minutes", -new Policy().get("51.price", 0))
            .param("reason", "Job announced to all users")
            .postTo(new ClaimsOf(this.farm, project));
        new Vacancies(this.farm).bootstrap().add(project, user, text);
        new ClaimOut()
            .type("Notify all")
            .author(user)
            .param(
                "message",
                new Par(
                    this.farm,
                    "Project %s is hiring, see [details](/p/%1$s);",
                    "@%s is inviting you to join them;",
                    "please, consider this project as an opportunity and",
                    "apply, as explained in §2;",
                    "this is what they say in their announcement:\n\n%s"
                ).say(project.pid(), user, text)
            )
            .param("min", new Policy().get("33.min-live", 0))
            .param("reason", "Project published")
            .postTo(new ClaimsOf(this.farm, project));
        return new RsForward(
            new RsParFlash(
                new Par(
                    "The job at %s announced to all our users, thanks!"
                ).say(project.pid()),
                Level.INFO
            ),
            String.format("/p/%s", project.pid())
        );
    }

}
