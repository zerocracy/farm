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

import com.zerocracy.Farm;
import com.zerocracy.Par;
import com.zerocracy.Policy;
import com.zerocracy.pm.ClaimOut;
import com.zerocracy.pmo.People;
import java.io.IOException;
import java.util.logging.Level;
import org.takes.Response;
import org.takes.facets.fork.RqRegex;
import org.takes.facets.fork.TkRegex;
import org.takes.facets.forward.RsForward;
import org.takes.rq.RqGreedy;
import org.takes.rq.form.RqFormSmart;

/**
 * Join Zerocracy form.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @author Kirill (g4s8.public@gmail.com)
 * @version $Id$
 * @since 0.20
 * @todo #800:30min Requests to /join should add an entry to 'resumes.xml'
 *  then an 'examiner' should be assigned. When the examiner "invites" a user,
 *  we check whether there was a resume for that user.
 *  If yes, we pay examiner +32 points. Examiner can reject a resume
 *  by saying 'deny {username}' (where username is a login in 'resumes.xml')
 *  to Zerocrat. In that case we also pay +32 to the examiner.
 *  Each user should see the status of his/her resume at /join.
 *  If the resume is there, he/she should see the resume, not the form.
 *  If he/she already has a mentor, there should be a redirect,
 *  saying that "User @yegor256 is already your mentor, no need to join again".
 *  See https://github.com/zerocracy/farm/issues/800#issuecomment-375551970
 *  comment for details.
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class TkJoin implements TkRegex {
    /**
     * Farm.
     */
    private final Farm farm;

    /**
     * Ctor.
     * @param frm Farm
     */
    public TkJoin(final Farm frm) {
        this.farm = frm;
    }

    @Override
    public Response act(final RqRegex req) throws IOException {
        final String author = new RqUser(this.farm, req, false).value();
        final People people = new People(this.farm).bootstrap();
        if (people.hasMentor(author)) {
            throw new RsForward(
                new RsParFlash(
                    new Par(
                        "You already have a mentor (@%s), why again?"
                    ).say(people.mentor(author)),
                    Level.WARNING
                ),
                "/join"
            );
        }
        final RqFormSmart form = new RqFormSmart(new RqGreedy(req));
        final String telegram = form.single("telegram");
        final String personality = form.single("personality");
        final String about = form.single("about");
        final int stko = Integer.parseInt(form.single("stackoverflow"));
        new ClaimOut().type("Join form submitted")
            .author(author)
            .param("telegram", telegram)
            .param("stackoverflow", stko)
            .param("about", about)
            .param("personality", personality)
            .postTo(this.farm);
        new ClaimOut().type("Notify all").param(
            "message", new Par(
                "A new user @%s (%s) would like to join us and needs a mentor;",
                "you can get in touch with him/her",
                "(telegram: `%s`) to discuss;",
                "if you become a mentor, you may earn",
                "some extra income, see §45;",
                "here are [GitHub](https://github.com/%1$s)",
                "and [StackOverflow](https://stackoverflow.com/users/%d)",
                "profiles of the user;",
                "this is the message the user left for us:\n\n%s"
            ).say(author, personality, telegram, stko, about)
        ).param("min", new Policy().get("1.min-rep", 0)).postTo(this.farm);
        return new RsForward(
            new RsParFlash(
                new Par(
                    "The request has been sent to all high-ranked users"
                ).say(),
                Level.INFO
            )
        );
    }
}
