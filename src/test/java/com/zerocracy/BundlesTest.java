/*
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
package com.zerocracy;

import com.jcabi.aspects.Tv;
import com.jcabi.xml.XML;
import com.jcabi.xml.XMLDocument;
import com.mongodb.client.model.Filters;
import com.zerocracy.claims.ClaimsItem;
import com.zerocracy.claims.Footprint;
import com.zerocracy.farm.SmartFarm;
import com.zerocracy.farm.StkSafe;
import com.zerocracy.farm.fake.FkFarm;
import com.zerocracy.farm.fake.FkProject;
import com.zerocracy.farm.reactive.Brigade;
import com.zerocracy.farm.reactive.StkGroovy;
import com.zerocracy.farm.reactive.StkRuntime;
import com.zerocracy.pmo.Catalog;
import com.zerocracy.pmo.Pmo;
import groovy.lang.Script;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import org.cactoos.Func;
import org.cactoos.io.InputOf;
import org.cactoos.io.InputWithFallback;
import org.cactoos.io.LengthOf;
import org.cactoos.io.OutputTo;
import org.cactoos.io.ResourceOf;
import org.cactoos.io.TeeInput;
import org.cactoos.iterable.Filtered;
import org.cactoos.iterable.Mapped;
import org.cactoos.iterable.Sorted;
import org.cactoos.list.ListOf;
import org.cactoos.list.SolidList;
import org.cactoos.scalar.And;
import org.cactoos.text.TextOf;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.reflections.Reflections;
import org.reflections.Store;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.SubTypesScanner;

/**
 * Test case for all bundles.
 *
 * <p>The concept of "bundles" is explained in the README.md. Each bundle
 * is a simulation of a real project, with some (!) items inside. Not
 * all items are present in each project, just some of them, which
 * are necessary for a particular testing scenario. All bundles
 * stay in {@code src/test/resources/com/zerocracy/bundles}.</p>
 *
 * <p>Here is how this test works. First, it copies all items into
 * {@code target/testing-bundles/[PID]} and into
 * {@code target/testing-bundles/PMO}. The first directory will
 * contain items for the project, the second one for the PMO during
 * the test.</p>
 *
 * <p>Before starting the test, {@code _before.groovy} runs, where
 * you can access the project an the PMO via {@code binding.variables}.
 * The best way to learn how it works is to study existing bundles.
 * After the tests are successfully completed,
 * {@code _after.groovy} runs.</p>
 *
 * <p>Logging is turned off by default. You can turn it on at
 * {@code src/test/resources/log4j.properties}
 * (see the instructions inside). Don't forget to turn it off before
 * commit.</p>
 *
 * <p>This video should help you understand how to run these tests
 * one by one: https://www.youtube.com/watch?v=oWEN-vKEEYk</p>
 *
 * @since 1.0
 * @todo #1364:30min This class is way to complex for a unit test. Most of the
 *  code here should be extracted into another class. And this
 *  new class should be covered with unit tests. And BundlesTess should be
 *  a simple class that just calls a method or two from the other class.
 *  Most of the checkstyle excludes should be removed after this refactoring.
 * @checkstyle JavadocMethodCheck (500 lines)
 * @checkstyle JavadocVariableCheck (500 lines)
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 * @checkstyle VisibilityModifierCheck (500 lines)
 * @checkstyle ClassFanOutComplexityCheck (500 lines)
 * @checkstyle DiamondOperatorCheck (500 lines)
 * @checkstyle ExecutableStatementCountCheck (500 lines)
 */
@SuppressWarnings
    (
        {
            "PMD.ExcessiveImports",
            "PMD.ExcessiveMethodLength",
            "PMD.AvoidInstantiatingObjectsInLoops"
        }
    )
@RunWith(Parameterized.class)
public final class BundlesTest {

    @Parameterized.Parameter
    public String bundle;

    private String name;

    private Path home;

    @BeforeClass
    public static void checkShouldRun() {
        Assume.assumeThat(
            "Parameter skipBundlesTest found, skipping...",
            System.getProperty("skipBundlesTest"),
            Matchers.nullValue()
        );
    }

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> bundles() {
        return new SolidList<Object[]>(
            new Mapped<>(
                path -> new Object[]{
                    path.substring(0, path.indexOf("/claims.xml")),
                },
                new Sorted<>(
                    new Reflections(
                        "com.zerocracy.bundles", new ResourcesScanner()
                    ).getResources(p -> p.endsWith("claims.xml"))
                )
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
        if (this.home.toFile().exists()) {
            Files.walk(this.home, FileVisitOption.FOLLOW_LINKS)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
        }
    }

    @Test
    public void oneBundleWorksFine() throws Exception {
        try (final Farm farm = new SmartFarm(
            new FkFarm(
                (Func<String, Project>) pid -> new FkProject(
                    this.home.resolve(pid), pid
                ),
                this.home.toString()
            )
        )) {
            final XML setup = new XMLDocument(
                new TextOf(
                    new InputWithFallback(
                        new ResourceOf(
                            String.format("%s/_setup.xml", this.bundle)
                        ),
                        new InputOf("<setup/>")
                    )
                ).asString()
            );
            final String pmo = "PMO";
            final String pid;
            final List<String> projects;
            if (setup.nodes("/setup/pmo").isEmpty()) {
                pid = this.name.toUpperCase(Locale.ENGLISH)
                    .replaceAll("[^A-Z0-9]", "")
                    .substring(0, Tv.NINE);
                projects = new ListOf<>(pid, pmo);
            } else {
                pid = pmo;
                projects = new ListOf<>(pmo);
            }
            final Project project = farm.find(
                String.format("@id='%s'", pid)
            ).iterator().next();
            final Catalog catalog = new Catalog(farm).bootstrap();
            catalog.add(pmo, String.format("%s/", pmo));
            if (!pmo.equals(pid)) {
                catalog.add(pid, String.format("2018/01/%s/", pid));
            }
            for (final String pfx : projects) {
                new And(
                    path -> {
                        new LengthOf(
                            new TeeInput(
                                new ResourceOf(path),
                                new OutputTo(
                                    this.home.resolve(pfx).resolve(
                                        path.substring(
                                            path.lastIndexOf(this.bundle)
                                                + this.bundle.length() + 1
                                        ).replaceFirst("^pmo_", "")
                                    )
                                )
                            )
                        ).intValue();
                    },
                    BundlesTest.resources(
                        this.bundle, pfx.equals(pmo) && !pid.equals(pmo)
                    )
                ).value();
            }
            new StkGroovy(
                new ResourceOf(
                    String.format("%s/_before.groovy", this.bundle)
                ),
                String.format("%s_before", this.bundle),
                farm
            ).process(project, null);
            BundlesTest.run(farm, project);
            new StkGroovy(
                new ResourceOf(
                    String.format("%s/_after.groovy", this.bundle)
                ),
                String.format("%s_after", this.bundle),
                farm
            ).process(project, null);
            try (final Footprint footprint = new Footprint(farm, project)) {
                MatcherAssert.assertThat(
                    "Error in footprint",
                    footprint.collection().find(
                        Filters.and(
                            Filters.eq("project", project.pid()),
                            Filters.eq("type", "Error")
                        )
                    ),
                    Matchers.emptyIterable()
                );
            }
        }
    }

    private static Iterable<String> resources(final String path,
        final boolean pmo) {
        final Store store = new Reflections(
            path.replace(File.separator, "."),
            new PatternScanner(
                new ResourcesScanner(),
                Pattern.compile(
                    String.format("^%s%s.*", path, File.separator)
                )
            )
        ).getStore();
        final String name = PatternScanner.class.getSimpleName();
        return store.get(
            name,
            new Filtered<>(
                p -> p.endsWith(".xml") && pmo == p.startsWith("pmo_"),
                store.get(name).keySet()
            )
        );
    }

    private static void run(final Farm farm, final Project project)
        throws IOException {
        final Brigade brigade = new Brigade(
            new Mapped<>(
                cls -> new StkSafe(
                    cls.getSimpleName(),
                    farm,
                    new StkRuntime(cls, farm)
                ),
                new Reflections(
                    "com.zerocracy.stk",
                    new SubTypesScanner(false)
                ).getSubTypesOf(Script.class)
            )
        );
        final Pmo pmo = new Pmo(farm);
        final ClaimsItem cpkt = new ClaimsItem(project).bootstrap();
        final ClaimsItem cpmo = new ClaimsItem(pmo).bootstrap();
        while (!cpkt.iterate().isEmpty() || !cpmo.iterate().isEmpty()) {
            cpkt.take(xml -> brigade.apply(project, xml));
            cpmo.take(xml -> brigade.apply(pmo, xml));
        }
    }
}
