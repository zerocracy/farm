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

import com.zerocracy.Item;
import com.zerocracy.Project;
import java.nio.file.Files;
import java.nio.file.Path;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link FkProject}.
 * @since 1.0
 * @checkstyle MagicNumber (500 lines)
 * @checkstyle JavadocMethodCheck (500 lines)
 */
public final class FkProjectTest {

    @Test
    public void implementsToString() throws Exception {
        MatcherAssert.assertThat(
            new FkProject().pid(),
            Matchers.equalTo("FAKEPRJCT")
        );
    }

    @Test
    public void differsByPathAndName() throws Exception {
        final Path dir = Files.createTempDirectory("ff1");
        MatcherAssert.assertThat(
            new FkProject(dir, "first.xml"),
            Matchers.not(Matchers.equalTo(new FkProject(dir, "second.xml")))
        );
    }

    @Test
    public void equalsWorks() throws Exception {
        final Path dir = Files.createTempDirectory("x1");
        MatcherAssert.assertThat(
            new FkProject(dir),
            Matchers.equalTo(new FkProject(dir))
        );
    }

    @Test
    public void managesItems() throws Exception {
        final Project project = new FkProject();
        final String name = "test.txt";
        try (final Item item = project.acq(name)) {
            Files.write(item.path(), "Hello, world!".getBytes());
        }
        try (final Item item = project.acq("something-else.txt")) {
            Files.write(item.path(), "Bye, bye!".getBytes());
        }
        try (final Item item = project.acq(name)) {
            MatcherAssert.assertThat(
                new String(Files.readAllBytes(item.path())),
                Matchers.startsWith("Hello")
            );
        }
    }

}
