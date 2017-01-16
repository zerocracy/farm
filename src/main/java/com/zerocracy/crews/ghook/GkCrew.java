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
package com.zerocracy.crews.ghook;

import com.jcabi.github.Github;
import com.zerocracy.jstk.Crew;
import com.zerocracy.jstk.Farm;
import java.io.IOException;
import java.util.Queue;
import javax.json.JsonObject;

/**
 * GitHub hook crew.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.7
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class GkCrew implements Crew {

    /**
     * Github client.
     */
    private final Github github;

    /**
     * Queue of incoming JSON events.
     */
    private final Queue<JsonObject> events;

    /**
     * Ctor.
     * @param ghb Github client
     * @param queue Queue
     */
    public GkCrew(final Github ghb, final Queue<JsonObject> queue) {
        this.github = ghb;
        this.events = queue;
    }

    @Override
    public void deploy(final Farm farm) throws IOException {
        while (true) {
            final JsonObject json = this.events.poll();
            if (json == null) {
                break;
            }
            System.out.println(json);
        }
    }

}
