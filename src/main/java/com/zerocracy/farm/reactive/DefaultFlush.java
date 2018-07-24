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
package com.zerocracy.farm.reactive;

import com.jcabi.log.Logger;
import com.jcabi.xml.XML;
import com.zerocracy.Farm;
import com.zerocracy.Project;
import com.zerocracy.entry.ClaimsOf;
import com.zerocracy.pm.ClaimIn;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.cactoos.BiFunc;
import org.cactoos.func.IoCheckedBiFunc;
import org.cactoos.iterable.Mapped;
import org.cactoos.text.JoinedText;
import org.cactoos.text.SubText;
import org.xembly.Directive;
import org.xembly.Directives;

/**
 * The action that happens in the {@link DefaultFlush}.
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
final class DefaultFlush implements Flush {

    /**
     * Claims limit for one take.
     */
    private static final int LIMIT = 5;

    /**
     * List of stakeholders.
     */
    private final IoCheckedBiFunc<Project, XML, Integer> brigade;

    /**
     * Farm.
     */
    private final Farm frm;

    /**
     * Ctor.
     * @param farm Farm
     * @param bgd Brigade
     */
    DefaultFlush(final Farm farm, final BiFunc<Project, XML, Integer> bgd) {
        this.frm = farm;
        this.brigade = new IoCheckedBiFunc<>(bgd);
    }

    @Override
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public void exec(final Project project) throws IOException {
        final AtomicInteger total = new AtomicInteger();
        new ClaimsOf(this.frm, project).take(
            xml -> this.process(project, xml, total),
            DefaultFlush.LIMIT
        );
    }

    @Override
    public void close() {
        // nothing
    }

    @Override
    public Iterable<Directive> value() {
        return new Directives();
    }

    /**
     * Process it.
     * @param project The project
     * @param xml The claim
     * @param idx Position in the queue
     * @throws IOException If fails
     */
    @SuppressWarnings("PMD.PrematureDeclaration")
    private void process(final Project project, final XML xml,
        final AtomicInteger idx) throws IOException {
        final long start = System.currentTimeMillis();
        final ClaimIn claim = new ClaimIn(xml);
        Logger.info(
            this,
            "Processing #%d:\"%s/%d\" at \"%s\"",
            idx.incrementAndGet(), claim.type(), claim.cid(), project.pid()
        );
        final int total = this.brigade.apply(project, xml);
        if (total == 0 && claim.hasToken()) {
            throw new IllegalStateException(
                String.format(
                    "Failed to process \"%s\"/\"%s\", no stakeholders",
                    claim.type(), claim.token()
                )
            );
        }
        Logger.info(
            this,
            "Seen #%d:\"%s/%d\" at \"%s\" by %d stk, %[ms]s [%s]%s",
            idx.get(), claim.type(), claim.cid(),
            project.pid(),
            total,
            System.currentTimeMillis() - start,
            new JoinedText(
                "; ",
                new Mapped<Map.Entry<String, String>, String>(
                    ent -> String.format(
                        "%s=%s", ent.getKey(),
                        // @checkstyle MagicNumber (1 line)
                        new SubText(ent.getValue(), 0, 120).asString()
                    ),
                    claim.params().entrySet()
                )
            ).asString(),
            // @checkstyle AvoidInlineConditionalsCheck (1 line)
            claim.hasAuthor() ? String.format(", by @%s", claim.author()) : ""
        );
    }

}
