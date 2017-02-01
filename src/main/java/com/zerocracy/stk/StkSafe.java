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

import com.jcabi.aspects.Tv;
import com.jcabi.log.Logger;
import com.jcabi.xml.XML;
import com.zerocracy.jstk.Project;
import com.zerocracy.jstk.Stakeholder;
import com.zerocracy.pm.ClaimIn;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.xembly.Directive;

/**
 * Stakeholder that reports about failures.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.1
 */
public final class StkSafe implements Stakeholder {

    /**
     * Original stakeholder.
     */
    private final Stakeholder origin;

    /**
     * Ctor.
     * @param stk Original stakeholder
     */
    public StkSafe(final Stakeholder stk) {
        this.origin = stk;
    }

    @Override
    @SuppressWarnings("PMD.AvoidCatchingThrowable")
    public Iterable<Directive> process(final Project project,
        final XML xml) throws IOException {
        Iterable<Directive> dirs;
        try {
            dirs = this.origin.process(project, xml);
        } catch (final SoftException ex) {
            dirs = new ClaimIn(xml).reply(ex.getMessage());
            // @checkstyle IllegalCatchCheck (1 line)
        } catch (final Throwable ex) {
            try (final ByteArrayOutputStream baos =
                new ByteArrayOutputStream()) {
                ex.printStackTrace(new PrintStream(baos));
                dirs = new ClaimIn(xml).reply(
                    String.join(
                        "\n",
                        "I can't do it for technical reasons, I'm very sorry.",
                        // @checkstyle LineLength (1 line)
                        "If you don't know what to do, email this to bug@0crat.com:\n\n```",
                        StringUtils.abbreviate(
                            baos.toString(StandardCharsets.UTF_8),
                            Tv.THOUSAND
                        ),
                        "```"
                    )
                );
                Logger.error(
                    this, "%s failed at \"%s/%s\": %s",
                    this.origin.getClass().getCanonicalName(),
                    new ClaimIn(xml).type(),
                    new ClaimIn(xml).number(),
                    ex.getLocalizedMessage()
                );
            }
        }
        return dirs;
    }

}
