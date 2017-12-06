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

import com.jcabi.github.Bulk;
import com.jcabi.github.Comment;
import com.jcabi.github.Comments;
import com.jcabi.github.Issue;
import com.jcabi.github.Smarts;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import org.cactoos.collection.Reversed;
import org.cactoos.list.SolidList;
import org.cactoos.text.SubText;

/**
 * Comments that don't allow more than 5 posts from us in a row.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.19
 */
public final class ThrottledComments implements Comments {

    /**
     * Original Github comments.
     */
    private final Comments comments;

    /**
     * Ctor.
     * @param cmts Comments
     */
    public ThrottledComments(final Comments cmts) {
        this.comments = cmts;
    }

    @Override
    public Comment post(final String text) throws IOException {
        final List<Comment.Smart> list = new SolidList<>(
            new Reversed<>(
                new Bulk<>(
                    new Smarts<>(
                        this.comments.iterate(new Date(0L))
                    )
                )
            )
        );
        final String self = this.comments.issue().repo().github().users()
            .self().login().toLowerCase(Locale.ENGLISH);
        boolean over = false;
        final int max = 5;
        for (int idx = 0; idx < list.size(); ++idx) {
            if (idx >= max) {
                over = true;
                break;
            }
            if (!list.get(idx).author().login().equalsIgnoreCase(self)) {
                break;
            }
        }
        if (over) {
            throw new IllegalStateException(
                String.format(
                    // @checkstyle LineLength (1 line)
                    "Can't post anything to %s#%d, too many comments already (over %d): %s",
                    this.comments.issue().repo().coordinates(),
                    this.comments.issue().number(),
                    max,
                    // @checkstyle MagicNumber (1 line)
                    new SubText(text, 0, 100).asString()
                )
            );
        }
        return this.comments.post(text);
    }

    @Override
    public Issue issue() {
        return this.comments.issue();
    }

    @Override
    public Comment get(final int number) {
        return this.comments.get(number);
    }

    @Override
    public Iterable<Comment> iterate(final Date since) {
        return this.comments.iterate(since);
    }

}
