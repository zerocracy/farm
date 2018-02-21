/**
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
package com.zerocracy.radars.github;

import com.jcabi.github.Github;
import com.zerocracy.Farm;
import java.io.IOException;
import javax.json.JsonObject;

/**
 * Milestone rebound.
 *
 * @author Kirill (g4s8.public@gmail.com)
 * @version $Id$
 * @since 0.21
 * @todo #186:30min Let's implement this `Rebound`. It should
 *  post a claim with type 'Add milestone' and params
 *  'milestone' and 'date' on github web-hook with new milestone.
 *  Milestone param is a label of milestone, date is a milestone date.
 */
public final class RbMilestone implements Rebound {
    @Override
    public String react(
        final Farm farm,
        final Github github,
        final JsonObject event
    ) throws IOException {
        throw new UnsupportedOperationException("#react()");
    }
}
