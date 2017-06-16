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
package com.zerocracy.entry;

import com.jcabi.aspects.Cacheable;
import com.jcabi.github.Github;
import com.jcabi.github.RtGithub;
import com.jcabi.github.mock.MkGithub;
import com.zerocracy.jstk.Farm;
import com.zerocracy.jstk.Project;
import com.zerocracy.pmo.Ext;
import com.zerocracy.pmo.Pmo;
import java.io.IOException;
import java.util.Map;
import lombok.EqualsAndHashCode;
import org.cactoos.Func;
import org.cactoos.Scalar;
import org.cactoos.func.IoCheckedFunc;
import org.cactoos.func.StickyFunc;

/**
 * GitHub server connector.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.7
 */
@EqualsAndHashCode(of = "pmo")
final class ExtGithub implements Scalar<Github> {

    /**
     * Cached test repos.
     */
    private static final Func<String, Github> FAKES = new StickyFunc<>(
        MkGithub::new
    );

    /**
     * PMO.
     */
    private final Project pmo;

    /**
     * Ctor.
     * @param farm Farm
     */
    public ExtGithub(final Farm farm) {
        this(new Pmo(farm));
    }

    /**
     * Ctor.
     * @param pkt Project
     */
    public ExtGithub(final Project pkt) {
        this.pmo = pkt;
    }

    @Override
    @Cacheable(forever = true)
    public Github asValue() throws IOException {
        final Ext ext = new Ext(this.pmo).bootstrap();
        final Map<String, String> props = ext.get("github");
        final String login = props.get("login");
        final Github github;
        if ("test".equals(login)) {
            github = new IoCheckedFunc<>(ExtGithub.FAKES).apply(login);
        } else {
            github = new RtGithub(login, props.get("password"));
        }
        return github;
    }

}
