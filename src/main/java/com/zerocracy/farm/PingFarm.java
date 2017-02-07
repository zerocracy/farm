/**
 * Copyright (c) 2016-2017 Zerocracy
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
import com.jcabi.aspects.Tv;
import com.zerocracy.jstk.Farm;
import com.zerocracy.jstk.Project;
import com.zerocracy.pm.ClaimOut;
import com.zerocracy.pm.Claims;
import java.io.IOException;

/**
 * Farm that pings all its projects every now and then.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.1
 */
@ScheduleWithFixedDelay(delay = Tv.FIFTEEN)
public final class PingFarm implements Runnable, Farm {

    /**
     * Original origin.
     */
    private final Farm origin;

    /**
     * Ctor.
     * @param frm Farm
     */
    public PingFarm(final Farm frm) {
        this.origin = frm;
    }

    @Override
    public Iterable<Project> find(final String xpath) throws IOException {
        return this.origin.find(xpath);
    }

    @Override
    public void run() {
        try {
            for (final Project project : this.origin.find("")) {
                PingFarm.ping(project);
            }
        } catch (final IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Ping one project.
     * @param project Project
     * @throws IOException If fails
     */
    private static void ping(final Project project) throws IOException {
        try (final Claims claims = new Claims(project).lock()) {
            claims.add(new ClaimOut().type("ping"));
        }
    }

}
