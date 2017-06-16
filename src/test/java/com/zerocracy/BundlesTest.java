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

import com.zerocracy.jstk.Farm;
import com.zerocracy.jstk.Item;
import com.zerocracy.jstk.Project;
import com.zerocracy.jstk.fake.FkFarm;
import com.zerocracy.pm.ClaimOut;
import com.zerocracy.pm.Claims;
import com.zerocracy.farm.SmartFarm;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import java.io.IOException;
import java.util.Collection;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import org.cactoos.Input;
import org.cactoos.func.AlwaysTrueFunc;
import org.cactoos.io.InputAsBytes;
import org.cactoos.io.LengthOfInput;
import org.cactoos.io.PathAsOutput;
import org.cactoos.io.ResourceAsInput;
import org.cactoos.io.TeeInput;
import org.cactoos.list.EndlessIterable;
import org.cactoos.list.IterableAsBoolean;
import org.cactoos.list.IterableAsList;
import org.cactoos.list.TransformedIterable;
import org.cactoos.text.BytesAsText;
import org.cactoos.text.FormattedText;
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
 */
@RunWith(Parameterized.class)
public final class BundlesTest {

    @Parameterized.Parameter
    public String bundle;

    @Parameterized.Parameters
    public static Collection<Object[]> bundles() {
        return new IterableAsList<>(
            new TransformedIterable<>(
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
    public void oneBundleWorksFine() throws IOException {
        final Farm farm = new SmartFarm(
            new FkFarm(),
            new Properties()
        ).asValue();
        final Project project = farm.find("id=12345").iterator().next();
        new IterableAsBoolean<>(
            BundlesTest.resources(this.bundle.replace("/", ".")),
            new AlwaysTrueFunc<>(
                path -> {
                    BundlesTest.save(
                        project,
                        new ResourceAsInput(path),
                        path.substring(path.lastIndexOf('/') + 1)
                    );
                }
            )
        ).asValue();
        new IterableAsBoolean<>(
            BundlesTest.resources("com.zerocracy.bundles._defaults"),
            new AlwaysTrueFunc<>(
                path -> {
                    BundlesTest.save(
                        project,
                        new ResourceAsInput(path),
                        path.substring(path.lastIndexOf('/') + 1)
                    );
                }
            )
        ).asValue();
        BundlesTest.script(
            String.format("%s/_before.groovy", this.bundle),
            project
        );
        new ClaimOut().type("ping").postTo(project);
        new IterableAsBoolean<>(
            new EndlessIterable<>(1),
            x -> {
                TimeUnit.SECONDS.sleep(1L);
                try (final Claims claims = new Claims(project).lock()) {
                    return !claims.iterate().isEmpty();
                }
            }
        ).asValue();
        BundlesTest.script(
            String.format("%s/_after.groovy", this.bundle),
            project
        );
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
            new LengthOfInput(
                new TeeInput(
                    input,
                    new PathAsOutput(item.path())
                )
            ).asValue();
        }
    }

    private static void script(final String script, final Project project)
        throws IOException {
        final Binding binding = new Binding();
        binding.setVariable("p", project);
        final GroovyShell shell = new GroovyShell(binding);
        shell.evaluate(
            new FormattedText(
                "%s\n\nexec(p)\n",
                new BytesAsText(
                    new InputAsBytes(
                        new ResourceAsInput(
                            script,
                            String.join(
                                "\n",
                                "import com.zerocracy.jstk.Project",
                                "def exec(Project p) {}"
                            )
                        )
                    )
                ).asString()
            ).asString()
        );
    }

}
