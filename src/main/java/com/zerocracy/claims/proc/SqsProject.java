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
package com.zerocracy.claims.proc;

import com.amazonaws.services.sqs.model.Message;
import com.zerocracy.Farm;
import com.zerocracy.Item;
import com.zerocracy.Project;
import java.io.IOException;
import org.cactoos.iterable.ItemAt;
import org.cactoos.scalar.IoCheckedScalar;
import org.cactoos.scalar.SolidScalar;

/**
 * Project from SQS message.
 *
 * @since 1.0
 */
public final class SqsProject implements Project {

    /**
     * Scalar.
     */
    private final IoCheckedScalar<Project> scalar;

    /**
     * Ctor.
     *
     * @param farm Farm
     * @param message SQS message
     */
    public SqsProject(final Farm farm, final Message message) {
        this.scalar = new IoCheckedScalar<>(
            new SolidScalar<>(
                () -> new ItemAt<>(
                    farm.find(
                        String.format(
                            "@id='%s'",
                            message.getMessageAttributes()
                                .get("project")
                                .getStringValue()
                        )
                    )
                ).value()
            )
        );
    }

    @Override
    public String pid() throws IOException {
        return this.scalar.value().pid();
    }
    @Override
    public Item acq(final String file) throws IOException {
        return this.scalar.value().acq(file);
    }
}
