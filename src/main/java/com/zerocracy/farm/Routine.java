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
package com.zerocracy.farm;

import com.jcabi.aspects.ScheduleWithFixedDelay;
import com.zerocracy.jstk.Crew;
import com.zerocracy.jstk.Farm;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Routine.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.1
 */
@ScheduleWithFixedDelay(delay = 1, unit = TimeUnit.MINUTES)
final class Routine implements Runnable {

    /**
     * Crew.
     */
    private final Crew crew;

    /**
     * Farm.
     */
    private final Farm farm;

    /**
     * Ctor.
     * @param crw Crew
     * @param frm Farm
     */
    Routine(final Crew crw, final Farm frm) {
        this.crew = crw;
        this.farm = frm;
    }

    @Override
    public void run() {
        try {
            this.crew.deploy(this.farm);
        } catch (final IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

}
