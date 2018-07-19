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
package com.zerocracy.farm.guts;

import com.jcabi.xml.XML;
import com.jcabi.xml.XMLDocument;
import com.zerocracy.Farm;
import com.zerocracy.Item;
import com.zerocracy.Project;
import java.io.IOException;
import org.cactoos.Func;
import org.cactoos.Scalar;
import org.cactoos.iterable.IterableOf;
import org.cactoos.scalar.IoCheckedScalar;
import org.cactoos.text.TextOf;
import org.xembly.Directive;

/**
 * Guts.
 *
 * @since 1.0
 */
public final class Guts implements
    Func<String, Iterable<Project>>, Scalar<XML> {

    /**
     * The query.
     */
    private static final String QUERY = "guts";

    /**
     * Farm.
     */
    private final Farm farm;

    /**
     * Normal path.
     */
    private final IoCheckedScalar<Iterable<Project>> normal;

    /**
     * Dirs to add.
     */
    private final IoCheckedScalar<Iterable<Directive>> dirs;

    /**
     * Ctor.
     * @param frm Farm
     */
    public Guts(final Farm frm) {
        this(frm, () -> new IterableOf<>(), () -> new IterableOf<>());
    }

    /**
     * Ctor.
     * @param frm Farm
     * @param norm Normal path
     * @param anex Dirs to add
     */
    public Guts(final Farm frm,
        final Scalar<Iterable<Project>> norm,
        final Scalar<Iterable<Directive>> anex) {
        this.farm = frm;
        this.normal = new IoCheckedScalar<>(norm);
        this.dirs = new IoCheckedScalar<>(anex);
    }

    @Override
    public Iterable<Project> apply(final String query) throws IOException {
        final Iterable<Project> list;
        if (Guts.QUERY.equals(query)) {
            list = new IterableOf<>(new GsProject(this.farm, query, this.dirs));
        } else {
            list = this.normal.value();
        }
        return list;
    }

    @Override
    public XML value() throws IOException {
        try (final Item item =
            this.farm.find(Guts.QUERY).iterator().next().acq("guts.xml")) {
            return new XMLDocument(new TextOf(item.path()).asString());
        }
    }

}
