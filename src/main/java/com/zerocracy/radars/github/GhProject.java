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
package com.zerocracy.radars.github;

import com.jcabi.github.Comment;
import com.jcabi.github.Repo;
import com.zerocracy.Farm;
import com.zerocracy.Item;
import com.zerocracy.Par;
import com.zerocracy.Project;
import com.zerocracy.SoftException;
import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;
import org.cactoos.Scalar;
import org.cactoos.scalar.IoCheckedScalar;
import org.cactoos.scalar.SolidScalar;

/**
 * Surrogate project.
 *
 * @since 1.0
 */
public final class GhProject implements Project {

    /**
     * Project.
     */
    private final Scalar<Project> pkt;

    /**
     * Ctor.
     * @param frm Farm
     * @param cmt Comment
     */
    public GhProject(final Farm frm, final Comment cmt) {
        this(frm, cmt.issue().repo());
    }

    /**
     * Ctor.
     * @param farm Farm
     * @param repo Repo
     */
    public GhProject(final Farm farm, final Repo repo) {
        this.pkt = new SolidScalar<>(
            () -> {
                final String name = repo.coordinates()
                    .toString().toLowerCase(Locale.ENGLISH);
                final Iterator<Project> list = farm.find(
                    String.format(
                        "links/link[@rel='github' and @href='%s']",
                        name
                    )
                ).iterator();
                if (!list.hasNext()) {
                    throw new SoftException(
                        new Par(
                            "I'm not managing `%s` GitHub repository,",
                            "you have to contact me in Slack first, see §11"
                        ).say(name)
                    );
                }
                return list.next();
            }
        );
    }

    @Override
    public String pid() throws IOException {
        return new IoCheckedScalar<>(this.pkt).value().pid();
    }

    @Override
    public Item acq(final String file) throws IOException {
        return new IoCheckedScalar<>(this.pkt).value().acq(file);
    }

}
