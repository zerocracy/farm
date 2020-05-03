/*
 * Copyright (c) 2016-2019 Zerocracy
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

import com.zerocracy.TextItem;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Test case for {@link FkItem}.
 * @since 1.0
 * @checkstyle MagicNumber (500 lines)
 * @checkstyle JavadocMethodCheck (500 lines)
 */
public final class FkItemTest {

    /**
     * Temp dir rule.
     */
    @Rule
    public final TemporaryFolder tmp = new TemporaryFolder();

    @Test
    public void implementsToString() throws Exception {
        final Path dir = Files.createTempDirectory("x1");
        final String name = "test.xml";
        final Path path = dir.resolve(name);
        MatcherAssert.assertThat(
            new FkItem(path).read(pth -> pth.getFileName().toString()),
            Matchers.equalTo(name)
        );
    }

    @Test
    public void equalsWorks() throws Exception {
        final Path dir = Files.createTempDirectory("x2");
        final String name = "testing.xml";
        final Path path = dir.resolve(name);
        MatcherAssert.assertThat(
            new FkItem(path),
            Matchers.equalTo(new FkItem(path))
        );
    }

    @Test
    public void returnsByContent() throws Exception {
        final Path path = this.tmp.newFile().toPath();
        Files.write(path, "hello, world!".getBytes(StandardCharsets.UTF_8));
        MatcherAssert.assertThat(
            new TextItem(new FkItem(path)).readAll(),
            Matchers.startsWith("hello, ")
        );
    }
}
