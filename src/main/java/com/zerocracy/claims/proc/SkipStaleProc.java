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
import com.zerocracy.claims.ClaimIn;
import java.time.Duration;
import java.time.Instant;
import org.cactoos.Proc;

/**
 * Skip stale claims.
 *
 * @since 1.0
 */
public final class SkipStaleProc implements Proc<Message> {

    /**
     * Expiration duration.
     */
    private static final Duration EXIRATION = Duration.ofMinutes(5L);

    /**
     * Origin proc.
     */
    private final Proc<Message> origin;

    /**
     * Ctor.
     *
     * @param origin Origin proc
     */
    public SkipStaleProc(final Proc<Message> origin) {
        this.origin = origin;
    }

    @Override
    public void exec(final Message input) throws Exception {
        final XML xml = new XMLDocument(input.getBody())
            .nodes("/claim").get(0);
        final ClaimIn claim = new ClaimIn(xml);
        if (SkipStaleProc.isStale(claim)) {
            Logger.warn(
                this, "Skipping stale claim: '%s'",
                claim.type()
            );
        } else {
            this.origin.exec(input);
        }
    }

    /**
     * Is claim stale.
     *
     * @param claim Claim
     * @return TRUE if stale
     */
    private static boolean isStale(final ClaimIn claim) {
        return "ping".equalsIgnoreCase(claim.type())
            && claim.created().toInstant().plus(SkipStaleProc.EXIRATION)
            .isBefore(Instant.now());
    }
}
