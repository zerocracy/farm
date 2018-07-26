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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import lombok.EqualsAndHashCode;

/**
 * Fake {@link Project}.
 *
 * <p>There is no thread-safety guarantee.</p>
 *
 * @since 1.0
 */
@EqualsAndHashCode(of = { "dir", "name" })
public final class FkProject implements Project {

    /**
     * Directory.
     * @since 1.0
     */
    private final Path dir;

    /**
     * Project name.
     * @since 1.0
     */
    private final String name;

    /**
     * All seen items.
     */
    private final Map<String, Item> items;

    /**
     * Ctor.
     * @throws IOException If fails
     * @since 1.0
     */
    public FkProject() throws IOException {
        this("FAKEPRJCT");
    }

    /**
     * Ctor.
     * @param file Location of files
     * @since 1.0
     */
    public FkProject(final Path file) {
        this(file, "FAKEPRJC2");
    }

    /**
     * Ctor.
     * @param pid Project name/id
     * @throws IOException If fails
     * @since 1.0
     */
    public FkProject(final String pid) throws IOException {
        this(Files.createTempDirectory("jstk").resolve(pid), pid);
    }

    /**
     * Ctor.
     * @param file Location of files
     * @param pid Project name/id
     * @since 1.0
     */
    public FkProject(final Path file, final String pid) {
        this.dir = file;
        this.items = new HashMap<>(0);
        this.name = pid;
    }

    @Override
    public String pid() {
        if (!this.name.matches("PMO|[0-9A-Z]{9}")) {
            throw new IllegalStateException(
                String.format("Project name is not valid: \"%s\"", this.name)
            );
        }
        return this.name;
    }

    @Override
    public Item acq(final String file) {
        if (!this.items.containsKey(file)) {
            this.items.put(file, new FkItem(this.dir.resolve(file)));
        }
        return this.items.get(file);
    }

}
