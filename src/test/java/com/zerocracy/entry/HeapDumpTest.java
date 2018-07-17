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
package com.zerocracy.entry;

import com.amazonaws.services.s3.model.ObjectMetadata;
import com.jcabi.s3.Ocket;
import com.jcabi.s3.fake.FkBucket;
import de.flapdoodle.embed.process.io.file.Files;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import org.cactoos.io.InputStreamOf;
import org.cactoos.text.TextOf;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Test for {@link HeapDump}.
 *
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class HeapDumpTest {
    /**
     * Temporary folder.
     */
    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    @Test
    public void savesDump() throws Exception {
        final File dir = this.temp.newFolder();
        final String prefix = "test-prefix:";
        final String file = "test-file";
        final String key = HeapDumpTest.key(prefix, file);
        final String content = "test-content";
        Files.write(content, new File(dir, key.replace(":", "_")));
        final FkBucket bkt = new FkBucket();
        new HeapDump(bkt, prefix, dir.toPath(), file)
            .save();
        MatcherAssert.assertThat(
            new Ocket.Text(bkt.ocket(key)).read(),
            Matchers.is(content)
        );
    }

    @Test
    public void loadsDump() throws Exception {
        final File dir = this.temp.newFolder();
        final String prefix = "test-prefix3:";
        final String file = "test-file3";
        final String content = "test-content3";
        Files.write(
            content,
            new File(dir, HeapDumpTest.key(prefix, file).replace(":", "_"))
        );
        final HeapDump dump = new HeapDump(
            new FkBucket(), prefix, dir.toPath(), file
        );
        dump.save();
        try (final InputStream load = dump.load()) {
            MatcherAssert.assertThat(
                new TextOf(load).asString(),
                Matchers.is(content)
            );
        }
    }

    @Test(expected = IOException.class)
    public void failsToLoadWhenNoDumpSaved() throws Exception {
        new HeapDump(
            new FkBucket(),
            "test-prefix5:",
            this.temp.newFolder().toPath(),
            "test-file5"
        ).load();
    }

    @Test
    public void doesNotSaveIfOlder() throws Exception {
        final File dir = this.temp.newFolder();
        final String prefix = "test-prefix2:";
        final String file = "test-file2";
        final String key = HeapDumpTest.key(prefix, file);
        final File old = new File(dir, key.replace(":", "_"));
        Files.write("old-content", old);
        old.setLastModified(1);
        final FkBucket bkt = new FkBucket();
        final String content = "new-content";
        bkt.ocket(key).write(
            new InputStreamOf(content),
            new ObjectMetadata()
        );
        new HeapDump(bkt, prefix, dir.toPath(), file)
            .save();
        MatcherAssert.assertThat(
            new Ocket.Text(bkt.ocket(key)).read(),
            Matchers.is(content)
        );
    }

    @Test(expected = IOException.class)
    public void failsIfFileDoesNotExist() throws Exception {
        new HeapDump(new FkBucket(), "foo:").save();
    }

    /**
     * S3 Key.
     * @param prefix Prefix
     * @param file File name
     * @return S3 Key string
     */
    private static String key(final String prefix, final String file) {
        return String.format("%s%s", prefix, file);
    }
}
