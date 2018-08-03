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
package com.zerocracy.farm.footprint;

import com.zerocracy.Farm;
import com.zerocracy.Project;
import com.zerocracy.claims.Footprint;
import java.io.IOException;
import lombok.EqualsAndHashCode;
import org.cactoos.iterable.Mapped;

/**
 * Footprint farm.
 *
 * <p>All claims are tracked through {@link Footprint}.</p>
 *
 * @since 1.0
 */
@EqualsAndHashCode(of = "origin")
public final class FtFarm implements Farm {

    /**
     * Original farm.
     */
    private final Farm origin;

    /**
     * Ctor.
     * @param farm Original farm
     */
    public FtFarm(final Farm farm) {
        this.origin = farm;
    }

    @Override
    public Iterable<Project> find(final String query) throws IOException {
        return new Mapped<>(
            project -> new FtProject(project, this.origin),
            this.origin.find(query)
        );
    }

    @Override
    public void close() throws IOException {
        this.origin.close();
    }
}
