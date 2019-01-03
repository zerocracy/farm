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
import com.zerocracy.pmo.Projects;
import java.io.IOException;
import org.cactoos.iterable.Mapped;
import org.cactoos.scalar.IoCheckedScalar;
import org.cactoos.scalar.Or;

/**
 * Roles of a user across all projects.
 * @since 1.0
 */
public final class ProjectsRoles {
    /**
     * Farm.
     */
    private final Farm farm;

    /**
     * User.
     */
    private final String login;

    /**
     * Ctor.
     * @param farm Farm
     * @param login User
     */
    public ProjectsRoles(final Farm farm, final String login) {
        this.farm = farm;
        this.login = login;
    }

    /**
     * Does user have the role(s) in any project?
     * @param list Roles to find
     * @return True, if user has roles in any project
     * @throws IOException If fails
     */
    public boolean hasRole(final String... list) throws IOException {
        return new IoCheckedScalar<>(
            new Or(
                new Mapped<>(
                    pid -> new IoCheckedScalar<>(
                        () -> new Roles(
                            this.farm.find(String.format("@id='%s'", pid))
                                .iterator().next()
                        ).bootstrap().hasRole(this.login, list)
                    ),
                    new Projects(this.farm, this.login).bootstrap().iterate()
                )
            )
        ).value();
    }

}
