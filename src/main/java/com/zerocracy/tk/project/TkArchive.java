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
package com.zerocracy.tk.project;

import com.jcabi.xml.XML;
import com.jcabi.xml.XMLDocument;
import com.zerocracy.Farm;
import com.zerocracy.Item;
import com.zerocracy.Par;
import com.zerocracy.Project;
import com.zerocracy.claims.ClaimOut;
import com.zerocracy.entry.ClaimsOf;
import com.zerocracy.tk.RqUser;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.cactoos.io.BytesOf;
import org.takes.Response;
import org.takes.facets.fork.RqRegex;
import org.takes.facets.fork.TkRegex;
import org.takes.rs.RsWithBody;
import org.takes.rs.RsWithType;

/**
 * Download the entire archive.
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class TkArchive implements TkRegex {

    /**
     * Farm.
     */
    private final Farm farm;

    /**
     * Ctor.
     * @param frm Farm
     */
    public TkArchive(final Farm frm) {
        this.farm = frm;
    }

    @Override
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public Response act(final RqRegex req) throws IOException {
        final Project project = new RqProject(this.farm, req, "PO");
        final XML list;
        try (final Item item = project.acq("_list.xml")) {
            list = new XMLDocument(item.path());
        }
        final Path zip = Files.createTempFile("0crat", ".zip");
        try (final ZipOutputStream out =
            new ZipOutputStream(new FileOutputStream(zip.toFile()))) {
            for (final String artifact : list.xpath("//item/name/text()")) {
                final ZipEntry entry = new ZipEntry(artifact);
                out.putNextEntry(entry);
                try (final Item item = project.acq(artifact)) {
                    final byte[] data = new BytesOf(item.path()).asBytes();
                    out.write(data, 0, data.length);
                }
                out.closeEntry();
            }
        }
        new ClaimOut().type("Notify PMO").param(
            "message", new Par(
                "Project %s was archived by @%s"
            ).say(project.pid(), new RqUser(this.farm, req, false).value())
        ).postTo(new ClaimsOf(this.farm));
        return new RsWithType(
            new RsWithBody(new BytesOf(zip).asBytes()),
            "application/zip"
        );
    }

}
