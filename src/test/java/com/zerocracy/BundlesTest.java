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
import com.zerocracy.jstk.Project;
import com.zerocracy.jstk.fake.FkFarm;
import com.zerocracy.pm.ClaimOut;
import com.zerocracy.pm.Claims;
import com.zerocracy.radars.telegram.TmSession;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.cactoos.io.LengthOf;
import org.cactoos.io.OutputTo;
import org.cactoos.io.ResourceOf;
import org.cactoos.io.TeeInput;
import org.cactoos.iterable.Endless;
import org.cactoos.iterable.Limited;
import org.cactoos.iterable.Mapped;
import org.cactoos.iterable.PropertiesOf;
import org.cactoos.iterable.Sorted;
import org.cactoos.list.StickyList;
import org.cactoos.map.MapEntry;
import org.cactoos.map.StickyMap;
import org.cactoos.scalar.And;
import org.cactoos.text.TextOf;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
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
 * @checkstyle ClassFanOutComplexityCheck (500 lines)
 */
@SuppressWarnings("PMD.ExcessiveImports")
@RunWith(Parameterized.class)
public final class BundlesTest {

    @Parameterized.Parameter
    public String bundle;

    private String name;

    private Path home;

    private FileAppender appender;

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> bundles() {
        return new StickyList<Object[]>(
            new Mapped<>(
                new Sorted<>(
                    new Reflections(
                        "com.zerocracy.bundles", new ResourcesScanner()
                    ).getResources(p -> p.endsWith("claims.xml"))
                ),
                path -> new Object[]{
                    path.substring(0, path.indexOf("/claims.xml")),
                }
            )
        );
    }

    @Before
    public void prepare() throws IOException {
        this.name = this.bundle.substring(
            this.bundle.lastIndexOf('/') + 1
        );
        this.home = Paths.get("target/testing-bundles")
            .resolve(this.name)
            .toAbsolutePath();
        Files.walk(this.home, FileVisitOption.FOLLOW_LINKS)
            .sorted(Comparator.reverseOrder())
            .map(Path::toFile)
            .forEach(File::delete);
        this.appender = new FileAppender(
            new PatternLayout("%t %p %m\n"),
            this.home.resolve("test.log").toString(),
            false
        );
        Logger.getRootLogger().addAppender(this.appender);
    }

    @After
    public void after() throws IOException {
        Logger.getRootLogger().removeAppender(this.appender);
        MatcherAssert.assertThat(
            String.format(
                "There were some exceptions in the log, see %s",
                this.appender.getFile()
            ),
            new TextOf(new File(this.appender.getFile())).asString(),
            Matchers.not(Matchers.containsString("Exception"))
        );
    }

    @Test
    @Ignore
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
        final String pid = this.name.toUpperCase(Locale.ENGLISH).replaceAll(
            "[^A-Z0-9]", ""
        ).substring(0, 9);
        final Farm farm = new SmartFarm(
            new FkFarm(this.home), props, deps
        ).value();
        final Project project = farm.find(
            String.format("@id='%s'", pid)
        ).iterator().next();
        new And(
            BundlesTest.resources(this.bundle.replace("/", ".")),
            path -> {
                new LengthOf(
                    new TeeInput(
                        new ResourceOf(path),
                        new OutputTo(
                            this.home.resolve(
                                path.substring(path.lastIndexOf('/') + 1)
                            )
                        )
                    )
                ).value();
            }
        ).value();
        new StkGroovy(
            new ResourceOf(
                String.format("%s/_before.groovy", this.bundle)
            ),
            String.format("%s_before", this.bundle),
            deps
        ).process(project, null);
        new ClaimOut().type("ping").postTo(project);
        MatcherAssert.assertThat(
            new And(
                new Limited<>(new Endless<>(1), Tv.FIFTY),
                x -> {
                    TimeUnit.SECONDS.sleep(1L);
                    final Claims claims = new Claims(project).bootstrap();
                    return !claims.iterate().isEmpty();
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

}
