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
package com.zerocracy.farm;

import com.zerocracy.farm.reactive.Brigade;
import com.zerocracy.farm.reactive.RvFarm;
import com.zerocracy.farm.reactive.StkGroovy;
import com.zerocracy.farm.sync.SyncFarm;
import com.zerocracy.jstk.Farm;
import com.zerocracy.jstk.Project;
import com.zerocracy.jstk.SoftException;
import com.zerocracy.jstk.Stakeholder;
import io.sentry.Sentry;
import java.util.Map;
import java.util.Properties;
import java.util.TreeSet;
import java.util.regex.Pattern;
import org.cactoos.Proc;
import org.cactoos.Scalar;
import org.cactoos.func.FuncWithFallback;
import org.cactoos.func.IoCheckedFunc;
import org.cactoos.io.ResourceAsInput;
import org.cactoos.list.MapEntry;
import org.cactoos.list.MappedIterable;
import org.cactoos.list.StickyMap;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;

/**
 * Smart farm.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.11
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@SuppressWarnings("PMD.ExcessiveImports")
public final class SmartFarm implements Scalar<Farm> {

    /**
     * Original farm.
     */
    private final Farm origin;

    /**
     * Properties.
     */
    private final Properties props;

    /**
     * Deps.
     */
    private final Map<String, Object> deps;

    /**
     * Ctor.
     * @param frm Original
     * @param pps Properties
     * @param dps Deps
     */
    public SmartFarm(final Farm frm, final Properties pps,
        final Map<String, Object> dps) {
        this.origin = frm;
        this.props = pps;
        this.deps = dps;
    }

    @Override
    public Farm value() {
        final Farm farm = new SyncFarm(this.origin);
        return new RvFarm(
            s -> new MappedIterable<>(
                farm.find(s),
                project -> new UplinkedProject(
                    new StrictProject(project),
                    farm
                )
            ),
            new Brigade(this.stakeholders())
        );
    }

    /**
     * List of stakeholders.
     * @return Stakeholders
     */
    private Iterable<Stakeholder> stakeholders() {
        return new MappedIterable<>(
            new TreeSet<>(
                new Reflections(
                    "com.zerocracy.stk", new ResourcesScanner()
                ).getResources(Pattern.compile(".*\\.groovy"))
            ),
            path -> new StkSafe(
                path,
                this.props,
                (project, xml) -> new IoCheckedFunc<>(
                    new FuncWithFallback<Project, Boolean>(
                        (Proc<Project>) pkt -> new StkGroovy(
                            new ResourceAsInput(path),
                            path,
                            new StickyMap<String, Object>(
                                this.deps,
                                new MapEntry<>("farm", this)
                            )
                        ).process(pkt, xml),
                        exp -> {
                            if (exp instanceof MismatchException) {
                                throw MismatchException.class.cast(exp);
                            }
                            if (exp instanceof SoftException) {
                                throw SoftException.class.cast(exp);
                            }
                            Sentry.capture(exp);
                        }
                    )
                ).apply(project)
            )
        );
    }
}
