/*
 * Copyright (c) 2016-2019 Zerocracy
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

import com.zerocracy.Farm;
import com.zerocracy.farm.fake.FkFarm;
import com.zerocracy.farm.fake.FkProject;
import com.zerocracy.pmo.Projects;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsEqual;
import org.junit.Test;

/**
 * Test case for {@link ProjectsRoles}.
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 */
public final class ProjectsRolesTest {

    @Test
    public void findsUsersRolesOnProjects() throws Exception {
        final FkProject project = new FkProject();
        final Roles roles = new Roles(project).bootstrap();
        final String uid = "yegor256";
        final String role = "ARC";
        roles.assign(uid, role);
        final Farm farm = new FkFarm(project);
        new Projects(farm, uid).bootstrap().add(project.pid());
        MatcherAssert.assertThat(
            new ProjectsRoles(farm, uid).hasRole(role),
            new IsEqual<>(true)
        );
    }
}
