/**
 * Copyright (c) 2016 Zerocracy
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
package com.zerocracy.pm.hr;

import com.zerocracy.jstk.fake.FkProject;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link Roles}.
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.1
 */
public final class RolesTest {

    /**
     * Prints them.
     * @throws Exception If some problem inside
     */
    @Test
    public void printsRoles() throws Exception {
        final Roles roles = new Roles(new FkProject());
        roles.bootstrap();
        final String person = "alex-palevsky";
        roles.assign(person, "PO");
        roles.assign(person, "DEV");
        MatcherAssert.assertThat(
            roles.markdown(),
            Matchers.containsString("alex-palevsky: PO, DEV")
        );
    }

    /**
     * Adds and removes roles.
     * @throws Exception If some problem inside
     */
    @Test
    public void addsAndRemovesRoles() throws Exception {
        final Roles roles = new Roles(new FkProject()).bootstrap();
        final String person = "davvd";
        final String role = "ARC";
        MatcherAssert.assertThat(
            roles.hasRole(person, role),
            Matchers.is(false)
        );
        roles.assign(person, role);
        MatcherAssert.assertThat(
            roles.hasRole(person, role),
            Matchers.is(true)
        );
        roles.resign(person, role);
        MatcherAssert.assertThat(
            roles.hasRole(person, role),
            Matchers.is(false)
        );
    }

    /**
     * Finds users by role.
     * @throws Exception If some problem inside
     */
    @Test
    public void findsUsersByRole() throws Exception {
        final Roles roles = new Roles(new FkProject()).bootstrap();
        final String uid = "yegor256";
        final String role = "QA";
        roles.assign(uid, role);
        MatcherAssert.assertThat(
            roles.findByRole(role),
            Matchers.hasItem(uid)
        );
    }

}
