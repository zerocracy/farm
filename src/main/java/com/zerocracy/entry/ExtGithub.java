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
package com.zerocracy.entry;

import com.jcabi.github.Github;
import com.jcabi.github.RtGithub;
import com.jcabi.github.mock.MkGithub;
import com.jcabi.http.wire.CachingWire;
import com.jcabi.http.wire.OneMinuteWire;
import com.jcabi.http.wire.RetryWire;
import com.zerocracy.Farm;
import com.zerocracy.farm.props.Props;
import java.io.IOException;
import org.cactoos.Scalar;
import org.cactoos.func.SolidFunc;
import org.cactoos.func.UncheckedFunc;

/**
 * GitHub server connector.
 *
 * @since 1.0
 */
public final class ExtGithub implements Scalar<Github> {

    /**
     * The singleton.
     */
    private static final UncheckedFunc<Farm, Github> SINGLETON =
        new UncheckedFunc<>(
            new SolidFunc<>(
                frm -> {
                    final Props props = new Props(frm);
                    final Github github;
                    if (props.has("//testing")) {
                        github = new MkGithub().relogin("test");
                    } else {
                        github = ExtGithub.prod(props);
                    }
                    return github;
                }
            )
        );

    /**
     * The farm.
     */
    private final Farm farm;

    /**
     * Ctor.
     * @param frm The farm
     */
    public ExtGithub(final Farm frm) {
        this.farm = frm;
    }

    @Override
    public Github value() {
        return ExtGithub.SINGLETON.apply(this.farm);
    }

    /**
     * Production GitHub client.
     * @param props Pros
     * @return Client
     * @throws IOException If fails
     */
    private static Github prod(final Props props) throws IOException {
        return new RtGithub(
            new RtGithub(
                props.get("//github//zerocrat.login"),
                props.get("//github//zerocrat.password")
            )
                .entry()
                .through(CachingWire.class, "(POST|PUT|PATCH) .*")
                .through(OneMinuteWire.class)
                .through(RetryWire.class)
        );
    }

}
