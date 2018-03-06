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
package com.zerocracy.tk;

import com.google.common.collect.Sets;
import com.zerocracy.Farm;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Set;
import java.util.logging.Level;
import org.takes.Request;
import org.takes.Response;
import org.takes.Take;
import org.takes.facets.forward.RsForward;
import org.takes.rs.RsWithBody;
import org.takes.rs.RsWithType;

/**
 * Out of memory heap dump.
 *
 * @author Kirill (g4s8.public@gmail.com)
 * @version $Id$
 * @since 0.20
 * @todo #425:30min Download and analyze heap dump from
 *  https://www.0crat.com/heapdump after next out of memory error,
 *  then fix detected memory leaks. Also waiting for #400 to be solved.
 * @todo #400:30min Should use HeapDump to get contents from S3,
 *  rather than from the server file, as it might be deleted and won't be
 *  available anymore.
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class TkDump implements Take {
    /**
     * Farm.
     */
    private final Farm farm;

    /**
     * Authorized users to look at dump.
     */
    private final Set<String> authorized;

    /**
     * Ctor.
     * @param frm Farm
     */
    public TkDump(final Farm frm) {
        this.authorized = Sets.newHashSet(
            "g4s8", "t-izbassar", "yegor256"
        );
        this.farm = frm;
    }

    @Override
    public Response act(final Request request) throws IOException {
        if (!this.authorized.contains(new RqUser(this.farm, request).value())) {
            throw new RsForward(
                new RsParFlash(
                    "You are not allowed to see this page, sorry.",
                    Level.WARNING
                )
            );
        }
        final File file = new File("./heapdump.hprof");
        if (!file.exists()) {
            throw new RsForward(
                new RsParFlash(
                    String.format("File doesn't exist: %s", file),
                    Level.SEVERE
                )
            );
        }
        return new RsWithType(
            new RsWithBody(
                new BufferedInputStream(
                    new FileInputStream(file)
                )
            ),
            "application/octet-stream"
        );
    }
}
