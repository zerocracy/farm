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
package com.zerocracy.tk;

import com.jcabi.aspects.Tv;
import com.jcabi.http.request.JdkRequest;
import com.jcabi.log.Logger;
import com.jcabi.log.VerboseRunnable;
import com.zerocracy.jstk.Farm;
import com.zerocracy.jstk.Project;
import com.zerocracy.pm.ClaimIn;
import com.zerocracy.pm.ClaimOut;
import com.zerocracy.pm.Claims;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;
import org.takes.Request;
import org.takes.Response;
import org.takes.Take;
import org.takes.rq.RqHref;
import org.takes.rs.RsText;

/**
 * Ping all projects.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.10
 */
public final class TkPing implements Take {

    /**
     * The type.
     */
    private static final String TYPE = "Ping";

    /**
     * Farm.
     */
    private final Farm farm;

    /**
     * Ctor.
     * @param frm Farm
     */
    public TkPing(final Farm frm) {
        this.farm = frm;
    }

    @Override
    public Response act(final Request req) throws IOException {
        final Collection<String> done = new LinkedList<>();
        final long start = System.currentTimeMillis();
        final ClaimOut out = new ClaimOut().type(TkPing.TYPE);
        for (final Project project : this.farm.find("")) {
            if (System.currentTimeMillis() - start >
                TimeUnit.SECONDS.toMillis((long) Tv.FIVE)) {
                done.add(TkPing.ping(req));
                break;
            }
            if (TkPing.needs(project)) {
                out.postTo(project);
                done.add(project.toString());
            } else {
                done.add(String.format("%s/not", project.toString()));
            }
        }
        return new RsText(
            Logger.format(
                "%d in %[ms]s: %s",
                done.size(),
                System.currentTimeMillis() - start,
                String.join("; ", done)
            )
        );
    }

    /**
     * This project needs a ping.
     * @param project The project
     * @return TRUE if needs a ping
     * @throws IOException If fails
     */
    private static boolean needs(final Project project) throws IOException {
        try (final Claims claims = new Claims(project).lock()) {
            return claims.iterate().stream().noneMatch(
                input -> new ClaimIn(input).type().equals(TkPing.TYPE)
            );
        }
    }

    /**
     * Ping itself.
     * @return Status
     */
    private static String ping(final Request req) {
        new Thread(
            new VerboseRunnable(
                () -> {
                    new JdkRequest(
                        new RqHref.Base(req).href().toString()
                    ).fetch();
                    return null;
                }
            )
        ).start();
        return "ping";
    }

}
