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
package com.zerocracy;

import com.jcabi.aspects.Tv;
import com.jcabi.github.mock.MkGithub;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.zerocracy.farm.SmartFarm;
import com.zerocracy.farm.reactive.StkGroovy;
import com.zerocracy.jstk.Farm;
import com.zerocracy.jstk.Item;
import com.zerocracy.jstk.Project;
import com.zerocracy.jstk.fake.FkFarm;
import com.zerocracy.pm.Claims;
import com.zerocracy.radars.telegram.TmSession;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import org.cactoos.Input;
import org.cactoos.io.LengthOf;
import org.cactoos.io.OutputTo;
import org.cactoos.io.ResourceOf;
import org.cactoos.io.TeeInput;
import org.cactoos.iterable.Endless;
import org.cactoos.iterable.Limited;
import org.cactoos.iterable.MapEntry;
import org.cactoos.iterable.Mapped;
import org.cactoos.iterable.PropertiesOf;
import org.cactoos.iterable.StickyList;
import org.cactoos.iterable.StickyMap;
import org.cactoos.scalar.And;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;

/**
 * Test case for all bundles.
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.11
 * @checkstyle JavadocMethodCheck (500 lines)
 * @checkstyle JavadocVariableCheck (500 lines)
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 * @checkstyle VisibilityModifierCheck (500 lines)
 * @todo #140:30m Optimize XsdResolver
 *  App spend a lot of time in `StrictXML.resolve` method.
 *  It seems that `XsdResolver` fetches schemas each time
 *  `Xocument` created. It may be more effective to download
 *  xsd schemas in build phase and read them instead of remote.
 * @todo #140:30m Optimize groovy scripts parsing
 *  Tests spend a lot of time on loading, parsing and compiling
 *  groovy stakeholder scripts. It would be more optimal to
 *  share `Brigade` instance between all bundle tests. It may be achieved
 *  using `@BeforeClass` JUnit annotation.
 * @todo #140:30m Optimize SyncItem.acquire() method
 *  App spend a lot of time on `acquire` method.
 *  It seems that we hold opened item too long or too often.
 *  Also we can share read-only item between multiple threads.
 */
@SuppressWarnings("PMD.ExcessiveImports")
@RunWith(Parameterized.class)
public final class BundlesTest {

    @Parameterized.Parameter
    public String bundle;

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> bundles() {
        return new StickyList<>(
            new Mapped<>(
                new Reflections(
                    "com.zerocracy.bundles", new ResourcesScanner()
                ).getResources(p -> p.endsWith("claims.xml")),
                path -> new Object[]{
                    path.substring(0, path.indexOf("/claims.xml")),
                }
            )
        );
    }

    @Test
    public void oneBundleWorksFine() throws Exception {
        final Properties props = new PropertiesOf(
            new MapEntry<>("testing", "true")
        ).value();
        // @checkstyle DiamondOperatorCheck (1 line)
        final Map<String, Object> deps = new StickyMap<String, Object>(
            new MapEntry<>("github", new MkGithub("test")),
            new MapEntry<>("slack", new HashMap<String, SlackSession>(0)),
            new MapEntry<>("telegram", new HashMap<Long, TmSession>(0)),
            new MapEntry<>("properties", props)
        );
        final Farm farm = new SmartFarm(new FkFarm(), props, deps).value();
        final Project project = farm.find("id=12345").iterator().next();
        new And(
            BundlesTest.resources(this.bundle.replace("/", ".")),
            path -> {
                BundlesTest.save(
                    project,
                    new ResourceOf(path),
                    path.substring(path.lastIndexOf('/') + 1)
                );
            }
        ).value();
        new StkGroovy(
            new ResourceOf(
                String.format("%s/_before.groovy", this.bundle)
            ),
            String.format("%s_before", this.bundle),
            deps
        ).process(project, null);
        MatcherAssert.assertThat(
            new And(
                new Limited<>(new Endless<>(1), Tv.FIFTY),
                x -> {
                    TimeUnit.SECONDS.sleep(1L);
                    try (final Claims claims = new Claims(project).lock()) {
                        return !claims.iterate().isEmpty();
                    }
                }
            ).value(),
            Matchers.equalTo(false)
        );
        new StkGroovy(
            new ResourceOf(
                String.format("%s/_after.groovy", this.bundle)
            ),
            String.format("%s_after", this.bundle),
            deps
        ).process(project, null);
    }

    private static Iterable<String> resources(final String pkg) {
        return new Reflections(
            pkg,
            new ResourcesScanner()
        ).getResources(p -> p.endsWith(".xml"));
    }

    private static void save(final Project project, final Input input,
        final String file) throws IOException {
        try (final Item item =
            project.acq(file.substring(file.lastIndexOf('/') + 1))) {
            new LengthOf(
                new TeeInput(
                    input,
                    new OutputTo(item.path())
                )
            ).value();
        }
    }

}
