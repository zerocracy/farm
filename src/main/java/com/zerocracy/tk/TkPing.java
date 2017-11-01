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

import com.jcabi.http.request.JdkRequest;
import com.jcabi.log.Logger;
import com.jcabi.log.VerboseThreads;
import com.zerocracy.jstk.Farm;
import com.zerocracy.jstk.Project;
import com.zerocracy.pm.ClaimIn;
import com.zerocracy.pm.ClaimOut;
import com.zerocracy.pm.Claims;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.cactoos.Func;
import org.cactoos.func.AsyncFunc;
import org.cactoos.iterable.Filtered;
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
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class TkPing implements Take {

    /**
     * The type.
     */
    private static final String TYPE = "Ping";

    /**
     * Executor service.
     */
    private final ExecutorService executor;

    /**
     * Farm.
     */
    private final Farm farm;

    /**
     * Ping counter.
     */
    private final AtomicInteger total;

    /**
     * Ctor.
     * @param frm Farm
     */
    public TkPing(final Farm frm) {
        this.farm = frm;
        this.total = new AtomicInteger();
        this.executor = Executors.newSingleThreadExecutor(
            new VerboseThreads(TkPing.class)
        );
    }

    @Override
    public Response act(final Request req) throws IOException {
        this.total.incrementAndGet();
        final Collection<String> done = new LinkedList<>();
        final long start = System.currentTimeMillis();
        final ClaimOut out = new ClaimOut().type(TkPing.TYPE);
        for (final Project project : this.farm.find("")) {
            if (System.currentTimeMillis() - start
                // @checkstyle MagicNumber (1 line)
                > TimeUnit.SECONDS.toMillis(5L)) {
                done.add(this.ping(req));
                break;
            }
            if (TkPing.needs(project)) {
                out.postTo(project);
                done.add(project.pid());
            } else {
                done.add(String.format("%s/not", project.pid()));
            }
        }
        return new RsText(
            Logger.format(
                "%d (%d) in %[ms]s: %s",
                done.size(),
                this.total.decrementAndGet(),
                System.currentTimeMillis() - start,
                String.join("; ", done)
            )
        );
    }

    /**
     * This project needs a run.
     * @param project The project
     * @return TRUE if needs a run
     * @throws IOException If fails
     */
    private static boolean needs(final Project project) throws IOException {
        final Claims claims = new Claims(project).bootstrap();
        return !new Filtered<>(
            claims.iterate(),
            input -> new ClaimIn(input).type().equals(TkPing.TYPE)
        ).iterator().hasNext();
    }

    /**
     * Ping itself.
     * @param req Request received
     * @return Status
     */
    private String ping(final Request req) {
        final String out;
        // @checkstyle MagicNumber (1 line)
        if (this.total.get() < 3) {
            final String arg = "loop";
            new AsyncFunc<>(
                (Func<Integer, Integer>) idx -> new JdkRequest(
                    new RqHref.Base(req)
                        .href()
                        .without(arg)
                        .with(arg, idx)
                        .toString()
                ).fetch().status(),
                this.executor
            ).apply(this.total.get());
            out = "re-ping";
        } else {
            out = String.format("Too many: %d", this.total.get());
        }
        return out;
    }

}
