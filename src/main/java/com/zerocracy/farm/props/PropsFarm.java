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
package com.zerocracy.farm.props;

import com.zerocracy.Farm;
import com.zerocracy.Project;
import com.zerocracy.farm.fake.FkFarm;
import com.zerocracy.farm.guts.Guts;
import java.io.IOException;
import lombok.EqualsAndHashCode;
import org.cactoos.iterable.Mapped;
import org.xembly.Directive;
import org.xembly.Directives;

/**
 * Props farm.
 *
 * <p>This {@link com.zerocracy.Farm} decorator will make sure all the Projects
 * have their {@code _props.xml} file loaded and accessible, by using the Props
 * class.</p>
 *
 * @since 1.0
 */
@EqualsAndHashCode(of = "origin")
public final class PropsFarm implements Farm {

    /**
     * Original farm.
     */
    private final Farm origin;

    /**
     * Post processing.
     */
    private final Iterable<Directive> post;

    /**
     * Ctor.
     */
    public PropsFarm() {
        this(new FkFarm());
    }

    /**
     * Ctor.
     * @param farm Original farm
     */
    public PropsFarm(final Farm farm) {
        this(farm, new Directives());
    }

    /**
     * Ctor.
     * @param dirs Post processing dirs
     */
    public PropsFarm(final Iterable<Directive> dirs) {
        this(new FkFarm(), dirs);
    }

    /**
     * Ctor.
     * @param farm Original farm
     * @param dirs Post processing dirs
     */
    public PropsFarm(final Farm farm, final Iterable<Directive> dirs) {
        this.origin = farm;
        this.post = dirs;
    }

    @Override
    public Iterable<Project> find(final String query) throws IOException {
        return new Guts(
            this.origin,
            () -> new Mapped<>(
                pkt -> new PropsProject(pkt, this.post),
                this.origin.find(query)
            ),
            () -> new Directives()
                .xpath("/guts")
                .add("farm")
                .attr("id", this.getClass().getSimpleName())
                .set(new Props(this).toString())
        ).apply(query);
    }

    @Override
    public void close() throws IOException {
        this.origin.close();
    }
}
