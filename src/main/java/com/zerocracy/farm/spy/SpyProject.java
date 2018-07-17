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

import com.zerocracy.Item;
import com.zerocracy.Project;
import java.io.IOException;
import lombok.EqualsAndHashCode;
import org.cactoos.Proc;
import org.cactoos.func.UncheckedProc;

/**
 * Spy {@link Project}.
 *
 * <p>There is no thread-safety guarantee.</p>
 *
 * @since 1.0
 */
@EqualsAndHashCode(of = "origin")
public final class SpyProject implements Project {

    /**
     * Origin.
     */
    private final Project origin;

    /**
     * Spy.
     */
    private final UncheckedProc<String> spy;

    /**
     * Ctor.
     * @param pkt The project
     * @param proc The spy
     */
    public SpyProject(final Project pkt, final Proc<String> proc) {
        this.origin = pkt;
        this.spy = new UncheckedProc<>(proc);
    }

    @Override
    public String pid() throws IOException {
        return this.origin.pid();
    }

    @Override
    public Item acq(final String file) throws IOException {
        this.spy.exec(String.format("acq:%s", file));
        return new SpyItem(this.origin.acq(file), this.spy);
    }

}
