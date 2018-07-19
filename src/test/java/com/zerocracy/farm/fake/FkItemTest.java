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
import java.nio.file.Files;
import java.nio.file.Path;
import org.cactoos.text.TextOf;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link FkItem}.
 * @since 1.0
 * @checkstyle MagicNumber (500 lines)
 * @checkstyle JavadocMethodCheck (500 lines)
 */
public final class FkItemTest {

    @Test
    public void createsInDefaultTempDir() throws Exception {
        try (final Item item = new FkItem()) {
            MatcherAssert.assertThat(
                item.toString(),
                Matchers.endsWith(".xml")
            );
        }
    }

    @Test
    public void implementsToString() throws Exception {
        final Path dir = Files.createTempDirectory("x1");
        final String name = "test.xml";
        final Path path = dir.resolve(name);
        try (final Item item = new FkItem(path)) {
            MatcherAssert.assertThat(
                item.toString(),
                Matchers.equalTo(name)
            );
        }
    }

    @Test
    public void equalsWorks() throws Exception {
        final Path dir = Files.createTempDirectory("x2");
        final String name = "testing.xml";
        final Path path = dir.resolve(name);
        try (final Item item = new FkItem(path)) {
            MatcherAssert.assertThat(
                item,
                Matchers.equalTo(new FkItem(path))
            );
        }
    }

    @Test
    public void returnsByContent() throws Exception {
        try (final Item item = new FkItem("hello, world!")) {
            MatcherAssert.assertThat(
                new TextOf(item.path()).asString(),
                Matchers.startsWith("hello, ")
            );
        }
    }

}
