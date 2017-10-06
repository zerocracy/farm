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
import com.zerocracy.farm.strict.StrictFarm;
import com.zerocracy.farm.sync.SyncFarm;
import com.zerocracy.farm.uplinked.UplinkedFarm;
import com.zerocracy.jstk.Farm;
import com.zerocracy.jstk.Stakeholder;
import java.util.Map;
import java.util.Properties;
import java.util.TreeSet;
import java.util.regex.Pattern;
import org.cactoos.Scalar;
import org.cactoos.io.ResourceOf;
import org.cactoos.iterable.Mapped;
import org.cactoos.map.MapEntry;
import org.cactoos.map.StickyMap;
import org.cactoos.scalar.StickyScalar;
import org.cactoos.scalar.SyncScalar;
import org.cactoos.scalar.UncheckedScalar;
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
     * Unique self.
     */
    private final Scalar<Farm> self;

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
     * @param farm Original
     * @param pps Properties
     * @param dps Deps
     */
    public SmartFarm(final Farm farm, final Properties pps,
        final Map<String, Object> dps) {
        this.props = pps;
        this.deps = dps;
        this.self = new SyncScalar<>(
            new StickyScalar<>(
                () -> new SyncFarm(
                    new RvFarm(
                        new UplinkedFarm(
                            new StrictFarm(farm)
                        ),
                        new Brigade(this.stakeholders())
                    )
                )
            )
        );
    }

    @Override
    public Farm value() {
        return new UncheckedScalar<>(this.self).value();
    }

    /**
     * List of stakeholders.
     * @return Stakeholders
     */
    private Iterable<Stakeholder> stakeholders() {
        return new Mapped<>(
            new TreeSet<>(
                new Reflections(
                    "com.zerocracy.stk", new ResourcesScanner()
                ).getResources(Pattern.compile(".*\\.groovy"))
            ),
            path -> new StkSafe(
                path,
                this.props,
                new StkGroovy(
                    new ResourceOf(path),
                    path,
                    new StickyMap<String, Object>(
                        this.deps,
                        new MapEntry<>("farm", this.value())
                    )
                )
            )
        );
    }
}
