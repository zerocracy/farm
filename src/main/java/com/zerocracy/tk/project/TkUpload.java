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

import com.jcabi.xml.StrictXML;
import com.jcabi.xml.XMLDocument;
import com.zerocracy.Farm;
import com.zerocracy.Item;
import com.zerocracy.Par;
import com.zerocracy.Project;
import com.zerocracy.claims.ClaimOut;
import com.zerocracy.entry.ClaimsOf;
import com.zerocracy.tk.RqUser;
import com.zerocracy.tk.RsParFlash;
import java.io.IOException;
import java.util.logging.Level;
import org.cactoos.io.LengthOf;
import org.cactoos.io.TeeInput;
import org.takes.Response;
import org.takes.facets.fork.RqRegex;
import org.takes.facets.fork.TkRegex;
import org.takes.facets.forward.RsForward;
import org.takes.rq.RqPrint;
import org.takes.rq.multipart.RqMtBase;
import org.takes.rq.multipart.RqMtSmart;

/**
 * Upload one file.
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class TkUpload implements TkRegex {

    /**
     * Farm.
     */
    private final Farm farm;

    /**
     * Ctor.
     * @param frm Farm
     */
    public TkUpload(final Farm frm) {
        this.farm = frm;
    }

    @Override
    public Response act(final RqRegex req) throws IOException {
        final RqMtSmart form = new RqMtSmart(new RqMtBase(req));
        final String artifact =
            new RqPrint(form.single("artifact")).printBody().trim();
        if (!artifact.matches("^[a-z]+\\.xml$")) {
            throw new RsForward(
                new RsParFlash(
                    new Par("Invalid artifact name \"%s\"").say(artifact),
                    Level.SEVERE
                )
            );
        }
        final Project project = new RqProject(this.farm, req, "PO");
        final String body =
            new RqPrint(form.single("file")).printBody().trim();
        try (final Item item = project.acq(artifact)) {
            new LengthOf(
                new TeeInput(
                    new StrictXML(new XMLDocument(body)).toString(),
                    item.path()
                )
            ).intValue();
        }
        new ClaimOut().type("Notify PMO").param(
            "message", new Par(
                "File `%s` uploaded manually to %s by @%s"
            ).say(artifact, project.pid(), new RqUser(this.farm, req).value())
        ).postTo(new ClaimsOf(this.farm));
        return new RsForward(
            new RsParFlash(
                new Par(
                    "File `%s` was uploaded to %s (%d chars)"
                ).say(artifact, project.pid(), body.length()),
                Level.INFO
            ),
            String.format("/files/%s", project.pid())
        );
    }

}
