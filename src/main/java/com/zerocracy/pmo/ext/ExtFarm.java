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
package com.zerocracy.pmo.ext;

import com.jcabi.aspects.Cacheable;
import com.jcabi.log.VerboseRunnable;
import com.jcabi.log.VerboseThreads;
import com.zerocracy.ThrowableToEmail;
import com.zerocracy.farm.StkSafe;
import com.zerocracy.farm.reactive.Brigade;
import com.zerocracy.farm.reactive.RvFarm;
import com.zerocracy.farm.reactive.StkGroovy;
import com.zerocracy.farm.sync.SyncFarm;
import com.zerocracy.jstk.Farm;
import com.zerocracy.jstk.Project;
import com.zerocracy.jstk.Stakeholder;
import com.zerocracy.jstk.fake.FkFarm;
import java.io.IOException;
import java.util.Properties;
import java.util.TreeSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.regex.Pattern;
import org.cactoos.Scalar;
import org.cactoos.func.FuncAsRunnable;
import org.cactoos.func.FuncWithCallback;
import org.cactoos.func.IoCheckedFunc;
import org.cactoos.func.ProcAsFunc;
import org.cactoos.func.RunnableAsFunc;
import org.cactoos.io.ResourceAsInput;
import org.cactoos.list.TransformedIterable;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;

/**
 * Farm bag.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.11
 */
public final class ExtFarm implements Scalar<Farm> {

    /**
     * Original farm.
     */
    private final Farm origin;

    /**
     * Ctor.
     */
    public ExtFarm() {
        this(new FkFarm());
    }

    /**
     * Ctor.
     * @param frm Original
     */
    public ExtFarm(final Farm frm) {
        this.origin = frm;
    }

    @Override
    @Cacheable(forever = true)
    public Farm asValue() throws IOException {
        final ThreadFactory factory = new VerboseThreads();
        final Properties props = new ExtProperties().asValue();
        return new RvFarm(
            new SyncFarm(this.origin),
            new Brigade(ExtFarm.stakeholders()),
            Executors.newSingleThreadExecutor(
                rnb -> factory.newThread(
                    new VerboseRunnable(
                        new FuncAsRunnable(
                            new FuncWithCallback<>(
                                new RunnableAsFunc<>(rnb),
                                new ThrowableToEmail(props)
                            )
                        ),
                        true, true
                    )
                )
            )
        );
    }

    public static Iterable<Stakeholder> stakeholders() {
        return new TransformedIterable<>(
            new TreeSet<>(
                new Reflections(
                    "com.zerocracy.stk", new ResourcesScanner()
                ).getResources(Pattern.compile(".*\\.groovy"))
            ),
            path -> new StkSafe(
                (project, xml) -> new IoCheckedFunc<>(
                    new FuncWithCallback<Project, Boolean>(
                        new ProcAsFunc<>(
                            pkt -> new StkGroovy(
                                new ResourceAsInput(path)
                            ).process(pkt, xml)
                        ),
                        new ThrowableToEmail()
                    )
                ).apply(project)
            )
        );
    }

}
