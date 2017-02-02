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
package com.zerocracy.stk;

import com.jcabi.xml.XML;
import com.zerocracy.jstk.Project;
import com.zerocracy.pm.ClaimIn;
import com.zerocracy.pm.hr.Roles;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

/**
 * Term to match by roles.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.1
 */
public final class TmRoles implements Term {

    /**
     * Roles to allow.
     */
    private final Iterable<String> roles;

    /**
     * Ctor.
     * @param list List of roles
     */
    public TmRoles(final String... list) {
        this(Arrays.asList(list));
    }

    /**
     * Ctor.
     * @param list List of roles
     */
    public TmRoles(final Iterable<String> list) {
        this.roles = list;
    }

    @Override
    public boolean fits(final Project project,
        final XML xml) throws IOException {
        if (this.has(project, xml)) {
            return true;
        }
        final ClaimIn claim = new ClaimIn(xml);
        if (claim.hasAuthor()) {
            final Collection<String> mine =
                new Roles(project).bootstrap().allRoles(claim.author());
            if (mine.isEmpty()) {
                throw new SoftException(
                    String.format(
                        // @checkstyle LineLength (1 line)
                        "You need to have one of these roles in order to do this: %s. I'm sorry to say this, but at the moment you've got no roles in this project (your GitHub login is \"%s\").",
                        TmRoles.join(this.roles), claim.author()
                    )
                );
            }
            throw new SoftException(
                String.format(
                    // @checkstyle LineLength (1 line)
                    "You can't do that, unless you have one of these roles: %s. Your current roles are: %s.",
                    TmRoles.join(this.roles),
                    TmRoles.join(mine)
                )
            );
        }
        throw new SoftException(
            String.format(
                // @checkstyle LineLength (1 line)
                "You're not allowed to do this, you need one of these roles: %s.",
                TmRoles.join(this.roles)
            )
        );
    }

    /**
     * Join by comma.
     * @param items Items
     * @return Joined
     */
    private static String join(final Iterable<String> items) {
        return String.join(", ", items);
    }

    /**
     * Has one of that required roles.
     * @param project Project
     * @param xml Claim
     * @return TRUE if he has
     * @throws IOException If fails
     */
    private boolean has(final Project project,
        final XML xml) throws IOException {
        boolean has = false;
        final ClaimIn claim = new ClaimIn(xml);
        if (claim.hasAuthor()) {
            final String login = claim.author();
            final Roles rls = new Roles(project).bootstrap();
            for (final String role : this.roles) {
                if (rls.hasRole(login, role)) {
                    has = true;
                    break;
                }
            }
        }
        return has;
    }

}
