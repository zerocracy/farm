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
package com.zerocracy.pmo.ext;

import com.jcabi.aspects.Cacheable;
import com.ullink.slack.simpleslackapi.SlackSession;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.cactoos.Scalar;

/**
 * Slack sessions.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.11
 */
public final class ExtSlack implements Scalar<Map<String, SlackSession>> {

    /**
     * Sessions.
     */
    private final Map<String, SlackSession> map;

    /**
     * Ctor.
     */
    public ExtSlack() {
        this.map = new ConcurrentHashMap<>(0);
    }

    @Override
    @Cacheable(forever = true)
    public Map<String, SlackSession> asValue() {
        return this.map;
    }

}
