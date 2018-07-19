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
package com.zerocracy.farm.fake;

import com.zerocracy.Farm;
import com.zerocracy.Item;
import com.zerocracy.Project;
import java.nio.file.Files;
import java.nio.file.Path;
import org.cactoos.Func;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link FkFarm}.
 * @since 1.0
 * @checkstyle MagicNumber (500 lines)
 * @checkstyle JavadocMethodCheck (500 lines)
 */
public final class FkFarmTest {

    @Test
    public void identityWorks() throws Exception {
        MatcherAssert.assertThat(
            new FkFarm(),
            Matchers.equalTo(new FkFarm())
        );
    }

    @Test
    public void giveProjectsCorrectnames() throws Exception {
        final String name = "A1BBCCDEF";
        MatcherAssert.assertThat(
            new FkFarm().find(
                String.format("@id='%s'", name)
            ).iterator().next().pid(),
            Matchers.equalTo(name)
        );
    }

    @Test
    public void retrievesProjectsByComplexXpath() throws Exception {
        MatcherAssert.assertThat(
            new FkFarm().find(
                "links/link[@rel='github' and @href='jeff/test']"
            ).iterator().next().pid(),
            Matchers.equalTo(new FkProject().pid())
        );
    }

    @Test
    public void findsByXpath() throws Exception {
        final Project project = new FkProject();
        final String xpath = "links/link[@rel='github' and @href='hey']";
        MatcherAssert.assertThat(
            new FkFarm(
                (Func<String, Project>) name -> project
            ).find(xpath).iterator().next(),
            Matchers.equalTo(project)
        );
    }

    @Test
    public void namesSubDirectoriesCorrectly() throws Exception {
        final Path dir = Files.createTempDirectory("x1");
        final Farm farm = new FkFarm(dir);
        try (final Item item =
            farm.find("@id='A77B88D99'").iterator().next().acq("t")) {
            Files.write(item.path(), "Just some text...".getBytes());
        }
        MatcherAssert.assertThat(
            Files.exists(dir.resolve("A77B88D99")),
            Matchers.is(true)
        );
    }

    @Test
    public void managesItems() throws Exception {
        final Farm farm = new FkFarm();
        final String pid = "@id='124342423'";
        final Project project = farm.find(pid).iterator().next();
        final String name = "test.txt";
        try (final Item item = project.acq(name)) {
            Files.write(item.path(), "Hello, world!".getBytes());
        }
        try (final Item item = project.acq("something-else.txt")) {
            Files.write(item.path(), "Bye, bye!".getBytes());
        }
        try (final Item item = farm.find(pid).iterator().next().acq(name)) {
            MatcherAssert.assertThat(
                new String(Files.readAllBytes(item.path())),
                Matchers.startsWith("Hello")
            );
        }
    }

    @Test
    public void keepsProjectsInUniqueDirs() throws Exception {
        final Farm farm = new FkFarm();
        final String name = "x.txt";
        final String first = "@id='AAAAAAAAA'";
        try (final Item item = farm.find(first).iterator().next().acq(name)) {
            Files.write(item.path(), "first project".getBytes());
        }
        try (final Item item =
            farm.find("@id='BBBBBBBBB'").iterator().next().acq(name)) {
            Files.write(item.path(), "second project".getBytes());
        }
        try (final Item item = farm.find(first).iterator().next().acq(name)) {
            MatcherAssert.assertThat(
                new String(Files.readAllBytes(item.path())),
                Matchers.startsWith("first")
            );
        }
    }

}
