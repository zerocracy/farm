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
package com.zerocracy.tk;

import com.zerocracy.Farm;
import com.zerocracy.pm.staff.Roles;
import com.zerocracy.pmo.Pmo;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import org.takes.Request;
import org.takes.Response;
import org.takes.Take;
import org.takes.facets.forward.RsForward;
import org.takes.rs.RsWithBody;
import org.takes.rs.RsWithHeader;

/**
 * This take allows PMO users to download app logs.
 * @since 1.0
 */
final class TkLog implements Take {

    /**
     * Farm.
     */
    private final Farm farm;

    /**
     * Ctor.
     * @param farm Farm
     */
    TkLog(final Farm farm) {
        this.farm = farm;
    }

    @Override
    public Response act(final Request req) throws IOException {
        final String user = new RqUser(this.farm, req).value();
        if (!new Roles(new Pmo(this.farm)).bootstrap().hasAnyRole(user)) {
            throw new RsForward(
                new RsParFlash(
                    "You are not allowed to download logs",
                    Level.WARNING
                )
            );
        }
        return new RsWithHeader(
            new RsWithBody(
                Files.readAllBytes(Paths.get("/tmp/log/farm.log"))
            ),
            "Content-Type", "application/octet-stream"
        );
    }
}
