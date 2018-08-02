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
import com.zerocracy.claims.Footprint;
import org.cactoos.Proc;

/**
 * Proc to add claim to footprint.
 *
 * @since 1.0
 */
public final class FootprintProc implements Proc<Message> {

    /**
     * Farm.
     */
    private final Farm farm;

    /**
     * Origin proc.
     */
    private final Proc<Message> origin;

    /**
     * Ctor.
     *
     * @param farm Farm
     * @param origin Origin
     */
    public FootprintProc(final Farm farm, final Proc<Message> origin) {
        this.farm = farm;
        this.origin = origin;
    }

    @Override
    public void exec(final Message input) throws Exception {
        try (
            final Footprint footprint = new Footprint(
                this.farm, new SqsProject(this.farm, input)
            )
        ) {
            final XML xml = new XMLDocument(input.getBody())
                .nodes("/claim").get(0);
            Logger.info(
                this, "Processing message %s",
                input.getMessageId()
            );
            footprint.open(
                xml,
                input.getMessageAttributes().get("signature").getStringValue()
            );
            Logger.info(
                this, "Claim was opened for message %s",
                input.getMessageId()
            );
            this.origin.exec(input);
            footprint.close(xml);
            Logger.info(
                this, "Claim was closed for message %s",
                input.getMessageId()
            );
        }
    }
}
