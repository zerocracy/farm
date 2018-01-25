/**
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
package com.zerocracy.err;

import com.zerocracy.Farm;
import com.zerocracy.SoftException;
import com.zerocracy.farm.props.Props;
import com.zerocracy.msg.TxtUnrecoverableError;
import java.io.IOException;
import org.cactoos.Proc;
import org.cactoos.func.IoCheckedProc;

/**
 * Send error text fallback.
 *
 * @author Kirill (g4s8.public@gmail.com)
 * @version $Id$
 * @since 0.21
 */
public final class FbSend implements ReFallback {
    /**
     * Message output.
     */
    private final Proc<String> out;
    /**
     * Properties.
     */
    private final Props props;

    /**
     * Ctor.
     * @param output Message output.
     * @param farm Project farm
     */
    public FbSend(final Proc<String> output, final Farm farm) {
        this(output, new Props(farm));
    }

    /**
     * Ctor.
     * @param output Message output
     * @param properties Properties
     */
    public FbSend(final Proc<String> output, final Props properties) {
        this.out = output;
        this.props = properties;
    }

    @Override
    public void process(final SoftException err) throws IOException {
        new IoCheckedProc<>(this.out).exec(err.getMessage());
    }

    @Override
    public void process(final Exception err) throws IOException {
        new IoCheckedProc<>(this.out).exec(
            new TxtUnrecoverableError(
                err,
                this.props
            ).asString()
        );
    }
}
