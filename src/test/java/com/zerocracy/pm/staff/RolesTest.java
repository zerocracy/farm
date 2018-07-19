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
package com.zerocracy.pm.staff;

import com.zerocracy.farm.fake.FkProject;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link Roles}.
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 */
public final class RolesTest {

    @Test
    public void addsAndRemovesRoles() throws Exception {
        final Roles roles = new Roles(new FkProject()).bootstrap();
        final String person = "davvd";
        final String role = "QA";
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

    @Test
    public void findsUsersByRole() throws Exception {
        final Roles roles = new Roles(new FkProject()).bootstrap();
        final String uid = "yegor256";
        roles.assign(uid, "TST");
        roles.assign("someone-else", "ARC");
        final String role = "QA";
        roles.assign(uid, role);
        MatcherAssert.assertThat(
            roles.findByRole(role),
            Matchers.allOf(
                Matchers.iterableWithSize(1),
                Matchers.hasItem(uid)
            )
        );
    }

}
