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

import com.jcabi.log.Logger;
import com.jcabi.log.VerboseThreads;
import com.zerocracy.Farm;
import com.zerocracy.Par;
import com.zerocracy.Project;
import com.zerocracy.pm.ClaimOut;
import com.zerocracy.pm.Claims;
import com.zerocracy.pmo.Catalog;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import org.cactoos.iterable.Shuffled;
import org.takes.Request;
import org.takes.Response;
import org.takes.Take;
import org.takes.facets.forward.RsForward;
import org.takes.rq.RqHref;
import org.takes.rs.RsText;

/**
 * Ping all projects.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.10
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class TkPing implements Take {

    /**
     * Executor service.
     */
    private final ExecutorService executor;

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
        this.executor = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors(),
            new VerboseThreads(TkPing.class)
        );
    }

    @Override
    public Response act(final Request req) throws IOException {
        final Collection<String> done = new LinkedList<>();
        final String type = new RqHref.Smart(req).single("type", "Ping");
        for (final Project project : new Shuffled<>(this.farm.find(""))) {
            done.add(this.post(project, type));
        }
        return new RsText(
            Logger.format(
                "%d done: %s",
                done.size(),
                String.join("; ", done)
            )
        );
    }

    /**
     * Post a ping.
     * @param project The project
     * @param type The type of claim to post
     * @return Summary
     * @throws IOException If fails
     */
    private String post(final Project project, final String type)
        throws IOException {
        if (!type.matches("Ping($| [a-z]+)")) {
            throw new RsForward(
                new RsParFlash(
                    new Par("Invalid claim type \"%s\"").say(type),
                    Level.SEVERE
                )
            );
        }
        final Claims claims = new Claims(project).bootstrap();
        final Catalog catalog = new Catalog(this.farm).bootstrap();
        final String out;
        if (catalog.exists(project.pid())) {
            if (catalog.pause(project.pid())) {
                out = String.format("%s/pause", project.pid());
            } else if (claims.iterate().isEmpty()) {
                this.executor.submit(
                    () -> {
                        new ClaimOut().type(type).postTo(project);
                        return null;
                    }
                );
                out = project.pid();
            } else {
                out = String.format("%s/busy", project.pid());
            }
        } else {
            out = String.format("%s/absent", project.pid());
        }
        return out;
    }

}
