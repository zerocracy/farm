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
package com.zerocracy.farm.guts;

import com.jcabi.xml.XML;
import com.jcabi.xml.XMLDocument;
import com.zerocracy.jstk.Farm;
import com.zerocracy.jstk.Item;
import com.zerocracy.jstk.Project;
import com.zerocracy.jstk.farm.fake.FkItem;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import org.cactoos.io.LengthOf;
import org.cactoos.io.TeeInput;
import org.cactoos.scalar.IoCheckedScalar;
import org.xembly.Directive;
import org.xembly.Xembler;

/**
 * Guts.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.19
 */
final class GsProject implements Project {

    /**
     * Farm.
     */
    private final Farm farm;

    /**
     * Query.
     */
    private final String query;

    /**
     * Dirs to add.
     */
    private final IoCheckedScalar<Iterable<Directive>> dirs;

    /**
     * Ctor.
     * @param frm Farm
     * @param qry The query
     * @param anex Dirs to add
     */
    GsProject(final Farm frm, final String qry,
        final IoCheckedScalar<Iterable<Directive>> anex) {
        this.farm = frm;
        this.query = qry;
        this.dirs = anex;
    }

    @Override
    public String pid() {
        return "";
    }

    @Override
    public Item acq(final String file) throws IOException {
        final Path temp = Files.createTempFile("farm", ".xml");
        final Iterator<Project> pkts = this.farm.find(this.query).iterator();
        XML before = new XMLDocument("<guts/>");
        if (pkts.hasNext()) {
            try (final Item item = pkts.next().acq(file)) {
                final Path path = item.path();
                if (Files.exists(path)
                    && path.toFile().length() != 0L) {
                    before = new XMLDocument(path);
                }
            }
        }
        new LengthOf(
            new TeeInput(
                new XMLDocument(
                    new Xembler(this.dirs.value()).applyQuietly(
                        before.node()
                    )
                ).toString(),
                temp
            )
        ).value();
        return new FkItem(temp);
    }
}
