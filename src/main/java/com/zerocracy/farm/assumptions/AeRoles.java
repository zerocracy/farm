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
package com.zerocracy.farm.assumptions;

import com.jcabi.xml.XML;
import com.zerocracy.jstk.Project;
import com.zerocracy.jstk.SoftException;
import com.zerocracy.pm.ClaimIn;
import com.zerocracy.pm.hr.Roles;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

/**
 * Assume roles.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.11
 */
public final class AeRoles {

    /**
     * Project.
     */
    private final Project project;

    /**
     * Claim.
     */
    private final XML xml;

    /**
     * Roles.
     */
    private final Collection<String> roles;

    /**
     * Ctor.
     * @param pkt Project
     * @param claim Claim
     * @param list List of roles
     */
    public AeRoles(final Project pkt, final XML claim,
        final String... list) {
        this.project = pkt;
        this.xml = claim;
        this.roles = Arrays.asList(list);
    }

    /**
     * At least one role is present.
     * @throws IOException If fails
     */
    public void exist() throws IOException {
        if (!this.has()) {
            final ClaimIn claim = new ClaimIn(this.xml);
            if (claim.hasAuthor()) {
                final Collection<String> mine = new Roles(this.project)
                    .bootstrap()
                    .allRoles(claim.author());
                if (mine.isEmpty()) {
                    throw new SoftException(
                        String.format(
                            // @checkstyle LineLength (1 line)
                            "You need to have one of these roles in order to do what you're trying to do: %s. I'm sorry to say this, but at the moment you've got no roles in this project (your GitHub login is \"%s\").",
                            String.join(", ", this.roles),
                            claim.author()
                        )
                    );
                }
                throw new SoftException(
                    String.format(
                        // @checkstyle LineLength (1 line)
                        "You can't do that, unless you have one of these roles: %s. Your current roles are: %s.",
                        String.join(", ", this.roles),
                        String.join(", ", mine)
                    )
                );
            }
            throw new SoftException(
                String.format(
                    // @checkstyle LineLength (1 line)
                    "You're not allowed to do this, you need one of these roles: %s.",
                    String.join(", ", this.roles)
                )
            );
        }
    }

    /**
     * Has one of that required roles.
     * @return TRUE if he has
     * @throws IOException If fails
     */
    private boolean has() throws IOException {
        boolean has = false;
        final ClaimIn claim = new ClaimIn(this.xml);
        if (claim.hasAuthor()) {
            final String login = claim.author();
            final Roles rls = new Roles(project).bootstrap();
            for (final String role : this.roles) {
                if (rls.hasRole(login, role)) {
                    has = true;
                    break;
                }
            }
        } else {
            has = true;
        }
        return has;
    }

}
