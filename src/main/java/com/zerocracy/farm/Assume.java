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
package com.zerocracy.farm;

import com.jcabi.xml.XML;
import com.zerocracy.Par;
import com.zerocracy.Project;
import com.zerocracy.SoftException;
import com.zerocracy.pm.ClaimIn;
import com.zerocracy.pm.staff.Roles;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import org.cactoos.collection.Mapped;
import org.cactoos.iterable.IterableOf;

/**
 * Assumptions for stakeholder.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.11
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class Assume {

    /**
     * Project.
     */
    private final Project project;

    /**
     * Claim.
     */
    private final XML xml;

    /**
     * Ctor.
     * @param pkt Project
     * @param claim Claim
     */
    public Assume(final Project pkt, final XML claim) {
        this.project = pkt;
        this.xml = claim;
    }

    /**
     * It's not a PMO.
     * @throws IOException If this is PMO
     */
    public void notPmo() throws IOException {
        if ("PMO".equals(this.project.pid())) {
            throw new MismatchException("This is PMO");
        }
    }

    /**
     * It's a PMO.
     * @throws IOException If this is PMO
     */
    public void isPmo() throws IOException {
        if (!"PMO".equals(this.project.pid())) {
            throw new MismatchException("This is not PMO");
        }
    }

    /**
     * Equals.
     * @param types The types to accept
     * @throws MismatchException If doesn't match
     */
    public void type(final String... types) throws MismatchException {
        final String input = new ClaimIn(this.xml)
            .type().toLowerCase(Locale.ENGLISH);
        final Set<String> expected = new HashSet<>(
            new Mapped<>(
                type -> type.toLowerCase(Locale.ENGLISH),
                new IterableOf<>(types)
            )
        );
        if (!expected.contains(input)) {
            throw new MismatchException(
                String.format(
                    "Type \"%s\" is not mine, I'm expecting \"%s\"",
                    input, expected
                )
            );
        }
    }

    /**
     * On of.
     * @param types The types to compare with
     * @throws MismatchException If doesn't match any
     */
    public void types(final Iterable<String> types) throws MismatchException {
        final String input = new ClaimIn(this.xml).type();
        for (final String type : types) {
            if (input.equalsIgnoreCase(type)) {
                return;
            }
        }
        throw new MismatchException(
            String.format(
                "Type \"%s\" is not mine, I'm expecting one of \"%s\"",
                input,
                types
            )
        );
    }

    /**
     * At least one role is present.
     * @param roles The roles
     * @throws IOException If fails
     */
    public void roles(final String... roles) throws IOException {
        if (this.hasRoles(roles)) {
            return;
        }
        final ClaimIn claim = new ClaimIn(this.xml);
        if (claim.hasAuthor()) {
            final Collection<String> mine = new Roles(this.project)
                .bootstrap()
                .allRoles(claim.author());
            if (mine.isEmpty()) {
                throw new SoftException(
                    new Par(
                        "You need to have one of these roles",
                        "in order to do what you're trying to do: %s.",
                        "I'm sorry to say this, but at the moment you've got",
                        "no roles in this project",
                        "(your GitHub login is \"%s\")."
                    ).say(String.join(", ", roles), claim.author())
                );
            }
            throw new SoftException(
                new Par(
                    "You can't do that, unless you have one of these roles:",
                    "%s. Your current roles are: %s."
                ).say(String.join(", ", roles), String.join(", ", mine))
            );
        }
        throw new SoftException(
            new Par(
                "You're not allowed to do this,",
                "you need one of these roles: %s."
            ).say(String.join(", ", roles))
        );
    }

    /**
     * Has one of that required roles.
     * @param roles Roles
     * @return TRUE if he has
     * @throws IOException If fails
     */
    private boolean hasRoles(final String... roles) throws IOException {
        boolean has = false;
        final ClaimIn claim = new ClaimIn(this.xml);
        if (claim.hasAuthor()) {
            final String login = claim.author();
            final Roles rls = new Roles(this.project).bootstrap();
            for (final String role : roles) {
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
