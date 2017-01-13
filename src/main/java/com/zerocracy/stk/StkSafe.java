/**
 * Copyright (c) 2016 Zerocracy
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
package com.zerocracy.stk;

import com.zerocracy.jstk.Stakeholder;
import com.zerocracy.pm.Person;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.output.ByteArrayOutputStream;

/**
 * Stakeholder that reports about failures.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.1
 */
public final class StkSafe implements Stakeholder {

    /**
     * Person.
     */
    private final Person person;

    /**
     * Original stakeholder.
     */
    private final Stakeholder origin;

    /**
     * Ctor.
     * @param prn Person
     * @param stk Original stakeholder
     */
    public StkSafe(final Person prn, final Stakeholder stk) {
        this.person = prn;
        this.origin = stk;
    }

    @Override
    @SuppressWarnings("PMD.AvoidCatchingThrowable")
    public void work() throws IOException {
        try {
            this.origin.work();
        } catch (final SoftException ex) {
            this.person.say(ex.getMessage());
            // @checkstyle IllegalCatchCheck (1 line)
        } catch (final Throwable ex) {
            try (final ByteArrayOutputStream baos =
                new ByteArrayOutputStream()) {
                ex.printStackTrace(new PrintStream(baos));
                this.person.say(
                    String.join(
                        "\n",
                        "I can't do it for technical reasons, I'm very sorry.",
                        // @checkstyle LineLength (1 line)
                        "If you don't know what to do, email this to bug@0crat.com:\n\n```",
                        baos.toString(StandardCharsets.UTF_8),
                        "```"
                    )
                );
            }
            throw new IOException(ex);
        }
    }

}
