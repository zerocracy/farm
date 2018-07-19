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
import com.zerocracy.Project;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.EqualsAndHashCode;
import org.cactoos.Func;
import org.cactoos.func.IoCheckedFunc;
import org.cactoos.func.StickyFunc;

/**
 * Fake {@link Farm}.
 *
 * <p>There is no thread-safety guarantee.</p>
 *
 * @since 1.0
 */
@EqualsAndHashCode(of = "fid")
public final class FkFarm implements Farm {

    /**
     * Name matching regexp.
     */
    private static final Pattern FINDER = Pattern.compile(
        "\\s*@id\\s*=\\s*[\"'](PMO|[A-Z0-9]{9})[\"']\\s*"
    );

    /**
     * Farm ID.
     */
    private final String fid;

    /**
     * Project finder (by name).
     */
    private final Func<String, Project> projects;

    /**
     * Ctor.
     */
    public FkFarm() {
        this(new StickyFunc<>(FkProject::new));
    }

    /**
     * Ctor to associate specific project with the name.
     * @param pkt Project to return
     * @param name Xpath
     */
    public FkFarm(final String name, final Project pkt) {
        this(Collections.singletonMap(name, pkt), name);
    }

    /**
     * Ctor.
     * @param dir Directory where files will be kept
     * @since 1.0
     */
    public FkFarm(final Path dir) {
        this(
            new StickyFunc<>(
                name -> new FkProject(
                    dir.resolve(name), name
                )
            ),
            dir.toString()
        );
    }

    /**
     * Ctor.
     * @param pkt Project to return
     * @since 1.0
     */
    public FkFarm(final Project pkt) {
        this((Func<String, Project>) s -> pkt);
    }

    /**
     * Ctor.
     * @param pkt Project to return
     * @param identifier Identifier
     * @since 1.0
     */
    public FkFarm(final Project pkt, final String identifier) {
        this((Func<String, Project>) s -> pkt, identifier);
    }

    /**
     * Ctor.
     * @param map Projects to initialize with
     */
    public FkFarm(final Map<String, Project> map) {
        this(map, FkFarm.class.getCanonicalName());
    }

    /**
     * Ctor.
     * @param map Projects to initialize with
     * @param identifier Identifier
     */
    public FkFarm(final Map<String, Project> map, final String identifier) {
        this(
            new StickyFunc<>(
                name -> {
                    final Project pkt = map.get(name);
                    if (pkt == null) {
                        throw new IllegalArgumentException(
                            String.format(
                                "Project \"%s\" is not in the farm",
                                name
                            )
                        );
                    }
                    return pkt;
                }
            ),
            identifier
        );
    }

    /**
     * Ctor.
     * @param func Mapping function
     * @since 1.0
     */
    public FkFarm(final Func<String, Project> func) {
        this(func, FkFarm.class.getCanonicalName());
    }

    /**
     * Ctor.
     * @param func Mapping function
     * @param identifier Identifier
     * @since 1.0
     */
    public FkFarm(final Func<String, Project> func, final String identifier) {
        this.projects = func;
        this.fid = identifier;
    }

    @Override
    public String toString() {
        return this.fid;
    }

    @Override
    public Iterable<Project> find(final String xpath) throws IOException {
        final Matcher matcher = FkFarm.FINDER.matcher(xpath);
        final String name;
        if (matcher.matches()) {
            name = matcher.group(1);
        } else {
            name = new FkProject().pid();
        }
        return Collections.singleton(
            new IoCheckedFunc<>(this.projects).apply(name)
        );
    }

    @Override
    public void close() {
        // nothing to do here
    }
}
