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
package com.zerocracy.claims;

import com.jcabi.xml.XML;
import com.zerocracy.Project;
import java.io.IOException;
import org.cactoos.Proc;
import org.cactoos.Scalar;
import org.cactoos.iterable.Endless;
import org.cactoos.iterable.Limited;
import org.cactoos.scalar.And;
import org.cactoos.scalar.IoCheckedScalar;

/**
 * XML claims implementation.
 *
 * @since 1.0
 */
public final class ClaimsXml implements Claims {

    /**
     * Take claims limit.
     */
    private static final int TAKE_LIMIT = 3;

    /**
     * Project.
     */
    private final Project pkt;

    /**
     * Ctor.
     *
     * @param project Project
     */
    public ClaimsXml(final Project project) {
        this.pkt = project;
    }

    /**
     * Take claims to process them.
     *
     * @param proc Proc func
     * @param limit Limit
     * @throws IOException If fails
     */
    public void take(final Proc<XML> proc, final int limit)
        throws IOException {
        final ClaimsItem item = new ClaimsItem(this.pkt).bootstrap();
        new IoCheckedScalar<>(
            new And(
                new Limited<>(
                    ClaimsXml.TAKE_LIMIT,
                    new Endless<Scalar<Boolean>>(() -> item.take(proc))
                )
            )
        ).value();
    }

    @Override
    public void submit(final XML claim) throws IOException {
        new ClaimsItem(this.pkt).bootstrap().add(claim);
    }
}
