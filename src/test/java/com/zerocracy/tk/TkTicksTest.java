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
import com.zerocracy.pulse.Pulse;
import com.zerocracy.pulse.Tick;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import javax.imageio.ImageIO;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.takes.Take;
import org.takes.rq.RqFake;
import org.takes.rs.RsPrint;

/**
 * Tests for {@link TkTicks}.
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class TkTicksTest {

    /**
     * {@link TkTicks} can render SVG.
     * @throws Exception If some problem inside
     */
    @Test
    public void rendersSvg() throws Exception {
        final Take home = new TkTicks(
            // @checkstyle AnonInnerLengthCheck (50 lines)
            new Pulse() {
                @Override
                public void add(final Tick tick) {
                    throw new UnsupportedOperationException("#add()");
                }
                @Override
                public Iterable<Tick> ticks() {
                    return Arrays.asList(
                        new Tick(1L, 1L, 1),
                        new Tick(2L, 1L, 1)
                    );
                }
                @Override
                public Iterable<Throwable> error() {
                    throw new UnsupportedOperationException("#error()");
                }
                @Override
                public void error(final Iterable<Throwable> errors) {
                    throw new UnsupportedOperationException("#error(..)");
                }
            }
        );
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        new RsPrint(home.act(new RqFake())).printBody(baos);
        final BufferedImage image = ImageIO.read(
            new ByteArrayInputStream(baos.toByteArray())
        );
        MatcherAssert.assertThat(
            image.getWidth(),
            Matchers.equalTo(Tv.THOUSAND)
        );
    }

    /**
     * {@link TkTicks} can render SVG without ticks.
     * @throws Exception If some problem inside
     */
    @Test
    public void rendersSvgWithoutTicks() throws Exception {
        final Take home = new TkTicks(new Pulse.Empty());
        MatcherAssert.assertThat(
            new RsPrint(home.act(new RqFake())).printBody(),
            Matchers.notNullValue()
        );
    }
}
