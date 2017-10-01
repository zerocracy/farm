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
package com.zerocracy.radars.github;

import com.jcabi.github.Github;
import com.jcabi.log.VerboseRunnable;
import com.jcabi.log.VerboseThreads;
import com.zerocracy.jstk.Farm;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.json.JsonObject;

/**
 * React when a new comment was posted.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.9
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class RbOnComment implements Rebound {

    /**
     * Runnable to trigger.
     */
    private final Runnable radar;

    /**
     * Service.
     */
    private final ExecutorService service;

    /**
     * Ctor.
     * @param code The radar
     */
    public RbOnComment(final Runnable code) {
        this.radar = code;
        this.service = Executors.newSingleThreadExecutor(
            new VerboseThreads()
        );
    }

    @Override
    public String react(final Farm farm, final Github github,
        final JsonObject event) {
        this.service.submit(
            new VerboseRunnable(
                () -> {
                    // @checkstyle MagicNumber (1 line)
                    TimeUnit.SECONDS.sleep(5L);
                    this.radar.run();
                    return null;
                },
                true, true
            )
        );
        return "Notification checking triggered";
    }

}
