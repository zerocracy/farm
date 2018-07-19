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
package com.zerocracy.pmo;

import com.zerocracy.Farm;
import com.zerocracy.Item;
import com.zerocracy.Xocument;
import java.io.IOException;
import org.xembly.Directives;

/**
 * Projects of a user.
 *
 * @since 1.0
 */
public final class Projects {

    /**
     * PMO.
     */
    private final Pmo pmo;

    /**
     * Login of the person.
     */
    private final String login;

    /**
     * Ctor.
     * @param farm The farm
     * @param user The user
     */
    public Projects(final Farm farm, final String user) {
        this(new Pmo(farm), user);
    }

    /**
     * Ctor.
     * @param pkt PMO
     * @param user The user
     */
    public Projects(final Pmo pkt, final String user) {
        this.pmo = pkt;
        this.login = user;
    }

    /**
     * Bootstrap it.
     * @return This
     * @throws IOException If fails
     */
    public Projects bootstrap() throws IOException {
        try (final Item item = this.item()) {
            new Xocument(item.path()).bootstrap("pmo/projects");
        }
        return this;
    }

    /**
     * Return full list of projects.
     * @return List of projects
     * @throws IOException If fails
     */
    public Iterable<String> iterate() throws IOException {
        try (final Item item = this.item()) {
            return new Xocument(item.path()).xpath(
                "/projects/project/text()"
            );
        }
    }

    /**
     * Add project.
     * @param pid Project ID
     * @throws IOException If fails
     */
    public void add(final String pid) throws IOException {
        try (final Item item = this.item()) {
            new Xocument(item.path()).modify(
                new Directives()
                    .xpath("/projects")
                    .add("project")
                    .set(pid)
            );
        }
    }

    /**
     * Remove project.
     * @param pid Project ID
     * @throws IOException If fails
     */
    public void remove(final String pid) throws IOException {
        try (final Item item = this.item()) {
            new Xocument(item.path()).modify(
                new Directives().xpath(
                    String.format(
                        "/projects/project[.='%s']",
                        pid
                    )
                ).remove()
            );
        }
    }

    /**
     * Return TRUE if it already exists.
     * @param pid Project ID
     * @return TRUE if exists
     * @throws IOException If fails
     */
    public boolean exists(final String pid) throws IOException {
        try (final Item item = this.item()) {
            return !new Xocument(item.path()).nodes(
                String.format(
                    "/projects/project[.='%s' ]",
                    pid
                )
            ).isEmpty();
        }
    }

    /**
     * The item.
     * @return Item
     * @throws IOException If fails
     */
    private Item item() throws IOException {
        return this.pmo.acq(
            String.format("projects/%s.xml", this.login)
        );
    }
}
