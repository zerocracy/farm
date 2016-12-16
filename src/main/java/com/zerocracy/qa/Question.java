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
package com.zerocracy.qa;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Question.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.1
 */
public final class Question {

    /**
     * Full text.
     */
    private final String text;

    /**
     * All possible options.
     */
    private final Iterable<Pattern> options;

    /**
     * Ctor.
     * @param txt Text
     * @param opts Options
     */
    public Question(final String txt, final String... opts) {
        this(txt, Arrays.asList(opts));
    }

    /**
     * Ctor.
     * @param txt Text
     * @param opts Options
     */
    public Question(final String txt, final Iterable<String> opts) {
        this.text = txt;
        this.options = StreamSupport.stream(opts.spliterator(), false)
            .map(Pattern::compile)
            .collect(Collectors.toList());
    }

    /**
     * Which option is it?
     * @return Matcher that matched
     */
    public Matcher option() {
        Matcher found = null;
        for (final Pattern opt : this.options) {
            final Matcher mtr = opt.matcher(this.text);
            if (mtr.matches()) {
                found = mtr;
                break;
            }
        }
        if (found == null) {
            throw new IllegalArgumentException(
                String.format("Can't understand: \"%s\"", this.text)
            );
        }
        return found;
    }

}
