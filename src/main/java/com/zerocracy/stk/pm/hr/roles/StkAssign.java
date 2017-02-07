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
package com.zerocracy.stk.pm.hr.roles;

import com.jcabi.github.Github;
import com.jcabi.http.Request;
import com.jcabi.http.response.RestResponse;
import com.jcabi.xml.XML;
import com.zerocracy.jstk.Project;
import com.zerocracy.jstk.Stakeholder;
import com.zerocracy.pm.ClaimIn;
import com.zerocracy.pm.hr.Roles;
import java.io.IOException;
import java.net.HttpURLConnection;
import org.xembly.Directive;

/**
 * Assign role to a person.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.1
 */
public final class StkAssign implements Stakeholder {

    /**
     * Github client.
     */
    private final Github github;

    /**
     * Ctor.
     * @param ghub Github client
     */
    public StkAssign(final Github ghub) {
        this.github = ghub;
    }

    @Override
    public Iterable<Directive> process(final Project project,
        final XML xml) throws IOException {
        final ClaimIn claim = new ClaimIn(xml);
        final String login = claim.param("login");
        this.github.entry().uri()
            .path("/user/following")
            .path(login)
            .back()
            .method(Request.PUT)
            .fetch()
            .as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_NO_CONTENT);
        final String role = claim.param("role");
        new Roles(project).bootstrap().assign(login, role);
        return claim.reply(
            String.format(
                "Role \"%s\" assigned to \"%s\" in \"%s\".",
                role,
                login,
                project
            )
        );
    }

}
