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
import com.jcabi.log.Logger;
import com.jcabi.xml.XML;
import com.jcabi.xml.XMLDocument;
import com.zerocracy.Farm;
import com.zerocracy.Project;
import com.zerocracy.claims.ClaimIn;
import com.zerocracy.farm.StkSafe;
import com.zerocracy.farm.StkVerbose;
import com.zerocracy.farm.reactive.Brigade;
import com.zerocracy.farm.reactive.StkRuntime;
import groovy.lang.Script;
import java.util.Map;
import org.cactoos.Proc;
import org.cactoos.iterable.Mapped;
import org.cactoos.text.JoinedText;
import org.cactoos.text.SubText;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;

/**
 * Process message in stakeholders brigade.
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class BrigadeProc implements Proc<Message> {

    /**
     * Stakeholders brigade.
     */
    private final Brigade brigade;

    /**
     * Farm.
     */
    private final Farm farm;

    /**
     * Ctor.
     *
     * @param farm Farm
     */
    public BrigadeProc(final Farm farm) {
        this(
            new Brigade(
                new Mapped<>(
                    cls -> new StkSafe(
                        cls.getSimpleName(),
                        farm,
                        new StkVerbose(
                            new StkRuntime(cls, farm),
                            cls.getName()
                        )
                    ),
                    new Reflections(
                        "com.zerocracy.stk",
                        new SubTypesScanner(false)
                    ).getSubTypesOf(Script.class)
                )
            ),
            farm
        );
    }

    /**
     * Ctor.
     *
     * @param brigade Brigade
     * @param farm Farm
     */
    public BrigadeProc(final Brigade brigade, final Farm farm) {
        this.brigade = brigade;
        this.farm = farm;
    }

    @Override
    @SuppressWarnings("PMD.PrematureDeclaration")
    public void exec(final Message input) throws Exception {
        final long start = System.currentTimeMillis();
        final XML xml = new XMLDocument(input.getBody())
            .nodes("/claim").get(0);
        final Project project = new SqsProject(this.farm, input);
        final ClaimIn claim = new ClaimIn(xml);
        Logger.info(
            this,
            "Processing message %s:\"%s/%s\" at \"%s\"",
            input.getMessageId(), claim.type(),
            claim.cid(), project.pid()
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
            "Seen message %s:\"%s/%s\" at \"%s\" by %d stk, %[ms]s [%s]%s",
            input.getMessageId(), claim.type(), claim.cid(),
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
