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
package com.zerocracy.tk;

import com.jcabi.aspects.Tv;
import com.jcabi.xml.XML;
import com.jcabi.xml.XMLDocument;
import com.jcabi.xml.XSL;
import com.jcabi.xml.XSLDocument;
import com.zerocracy.pulse.Pulse;
import com.zerocracy.pulse.Tick;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.takes.Request;
import org.takes.Response;
import org.takes.Take;
import org.takes.rs.RsWithBody;
import org.takes.rs.RsWithType;
import org.w3c.dom.Document;
import org.xembly.Directives;
import org.xembly.Xembler;

/**
 * {@link Take} for {@link Pulse} SVG.
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
final class TkTicks implements Take {

    /**
     * XSLT for pulse render.
     */
    private static final XSL PULSE = XSLDocument.make(
        TkTicks.class.getResourceAsStream("pulse.xsl")
    );

    /**
     * Pulse.
     */
    private final Pulse pulse;

    /**
     * Ctor.
     * @param pulse Pulse
     */
    TkTicks(final Pulse pulse) {
        this.pulse = pulse;
    }

    @Override
    public Response act(final Request req) throws IOException {
        return new RsWithType(
            new RsWithBody(this.png()),
            "image/png"
        );
    }

    /**
     * Make PNG in bytes.
     * @return Bytes
     * @throws IOException If fails
     */
    private byte[] png() throws IOException {
        final TranscoderInput input = new TranscoderInput(
            Document.class.cast(
                TkTicks.PULSE.transform(this.dirs()).node()
            )
        );
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final TranscoderOutput output = new TranscoderOutput(baos);
        final PNGTranscoder transcoder = new PNGTranscoder();
        transcoder.addTranscodingHint(
            PNGTranscoder.KEY_WIDTH, (float) Tv.THOUSAND
        );
        transcoder.addTranscodingHint(
            PNGTranscoder.KEY_HEIGHT, (float) Tv.HUNDRED
        );
        try {
            transcoder.transcode(input, output);
        } catch (final TranscoderException ex) {
            throw new IOException(ex);
        }
        return baos.toByteArray();
    }

    /**
     * Turn ticks into XML.
     * @return XML
     */
    private XML dirs() {
        final long now = System.currentTimeMillis();
        final Directives dirs = new Directives().add("pulse");
        final Iterator<Tick> iterator = this.pulse.ticks().iterator();
        while (iterator.hasNext()) {
            final Tick tick = iterator.next();
            dirs.add("tick")
                .attr("total", Integer.toString(tick.total()))
                .attr("start", Long.toString(tick.start() - now))
                .attr("msec", Long.toString(tick.duration()))
                .up();
        }
        return new XMLDocument(new Xembler(dirs).xmlQuietly());
    }
}
