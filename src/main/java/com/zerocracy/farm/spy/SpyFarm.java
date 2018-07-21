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
package com.zerocracy.farm.spy;

import com.zerocracy.Farm;
import com.zerocracy.Project;
import java.io.IOException;
import lombok.EqualsAndHashCode;
import org.cactoos.Proc;
import org.cactoos.func.UncheckedProc;
import org.cactoos.iterable.Mapped;

/**
 * Fake {@link Farm}.
 *
 * <p>There is no thread-safety guarantee.</p>
 *
 * @since 1.0
 */
@EqualsAndHashCode(of = "origin")
public final class SpyFarm implements Farm {

    /**
     * Farm.
     */
    private final Farm origin;

    /**
     * Proc to accept changes.
     */
    private final UncheckedProc<String> spy;

    /**
     * Ctor.
     * @param farm The farm
     * @param proc The spy
     */
    public SpyFarm(final Farm farm, final Proc<String> proc) {
        this.origin = farm;
        this.spy = new UncheckedProc<>(proc);
    }

    @Override
    public String toString() {
        return this.origin.toString();
    }

    @Override
    public Iterable<Project> find(final String xpath) throws IOException {
        this.spy.exec(String.format("find:%s", xpath));
        return new Mapped<>(
            project -> new SpyProject(project, this.spy),
            this.origin.find(xpath)
        );
    }

    @Override
    public void close() throws IOException {
        this.origin.close();
    }
}
