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
package com.zerocracy.pm.staff.votes;

import com.zerocracy.Project;
import com.zerocracy.pmo.Verbosity;
import java.util.Collection;
import java.util.Comparator;
import org.cactoos.map.MapEntry;
import org.cactoos.map.MapOf;
import org.cactoos.map.SolidMap;

/**
 * Vote for user if they have a lot of comments in ticket (verobose).
 * The more verbose is the person
 * the less likely he/she should get the next job.
 *
 * @since 1.0
 */
@SuppressWarnings({"PMD.SingularField", "PMD.UnusedPrivateField"})
public final class VsVerbosity extends VsRank<Integer> {

    /**
     * Ctor.
     * @param pmo PMO project.
     * @param others Other logins
     */
    public VsVerbosity(final Project pmo, final Collection<String> others) {
        super(
            new SolidMap<>(
                new MapOf<>(
                    login -> new MapEntry<>(
                        login,
                        new Verbosity(pmo, login)
                            .bootstrap().messages()
                    ),
                    others
                )
            ),
            Comparator.naturalOrder()
        );
    }
}
