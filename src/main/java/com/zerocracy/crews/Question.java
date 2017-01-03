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
package com.zerocracy.crews;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Question in text.
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
     * Ctor.
     * @param txt Text to parse
     */
    public Question(final String txt) {
        this.text = txt;
    }

    /**
     * Get argument by name.
     * @param name The name
     * @return Value
     */
    public String arg(final String name) {
        final Matcher matcher = Pattern.compile(
            String.format(
                "%s\\s*(?:=|is)\\s*`([^`]+)`", name
            )
        ).matcher(this.text);
        if (!matcher.matches()) {
            throw new IllegalArgumentException(
                String.format(
                    "Argument \"%s\" not found in \"%s\"", name, this.text
                )
            );
        }
        return matcher.group(1);
    }

}
