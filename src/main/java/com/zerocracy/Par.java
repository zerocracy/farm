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
package com.zerocracy;

import com.jcabi.log.Logger;
import com.zerocracy.farm.fake.FkFarm;
import com.zerocracy.pmo.Catalog;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.cactoos.Func;
import org.cactoos.func.IoCheckedFunc;
import org.cactoos.list.ListOf;
import org.cactoos.text.JoinedText;

/**
 * Smart paragraph of text.
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class Par {

    /**
     * The farm.
     */
    private final Farm farm;

    /**
     * The text parts.
     */
    private final Iterable<String> parts;

    /**
     * Ctor.
     * @param list The text
     */
    public Par(final String... list) {
        this(new FkFarm(), list);
    }

    /**
     * Ctor.
     * @param frm The farm
     * @param list The text
     */
    public Par(final Farm frm, final String... list) {
        this.farm = frm;
        this.parts = new ListOf<>(list);
    }

    /**
     * Print it.
     * @param args The args
     * @return Text
     * @throws IOException If fails
     */
    public String say(final Object... args) throws IOException {
        String out = Logger.format(
            new JoinedText(" ", this.parts).asString(), args
        );
        out = Par.replace(
            out, Par.pattern("@([a-z-0-9]{3,})"),
            matcher -> String.format(
                "%s[/z](https://www.0crat.com/u/%s)",
                matcher.group(0), matcher.group(1)
            )
        );
        out = Par.replace(
            out, Par.pattern("(C[A-Z0-9]{8})"),
            matcher -> {
                String title = matcher.group(0);
                final Catalog catalog = new Catalog(this.farm).bootstrap();
                if (catalog.exists(title)) {
                    title = catalog.title(title);
                }
                return String.format(
                    "[%s](https://www.0crat.com/p/%s)",
                    title, matcher.group(0)
                );
            }
        );
        out = Par.replace(
            out,
            Par.pattern("gh:([a-zA-Z0-9-]+/[a-zA-Z0-9-.]+)#(\\d+)"),
            matcher -> String.format(
                "[#%s](https://github.com/%s/issues/%1$s)",
                matcher.group(2), matcher.group(1)
            )
        );
        out = Par.replace(
            out, Par.pattern("(\\d+) ([a-z]+)\\(s\\)"),
            matcher -> {
                final int count = Integer.parseInt(matcher.group(1));
                final String txt;
                if (count == 1) {
                    txt = String.format("one %s", matcher.group(2));
                } else {
                    txt = String.format("%d %ss", count, matcher.group(2));
                }
                return txt;
            }
        );
        out = Par.replace(
            out, Par.pattern("§(\\d+)"),
            matcher -> String.format(
                "[%s](http://www.zerocracy.com/policy.html#%s)",
                matcher.group(0), matcher.group(1)
            )
        );
        out = Par.replace(
            out, Par.pattern("(\\[[^]]+])\\((/\\d{4}/[^)]+)\\)"),
            matcher -> String.format(
                "%s(http://www.yegor256.com%s)",
                matcher.group(1), matcher.group(2)
            )
        );
        out = Par.replace(
            out, Par.pattern("(\\[[^]]+])\\((/[^)]+)\\)"),
            matcher -> String.format(
                "%s(https://www.0crat.com%s)",
                matcher.group(1), matcher.group(2)
            )
        );
        out = Par.replace(
            out, Par.pattern("(ARC|DEV|REV|PO|QA|TST|HLP)"),
            matcher -> String.format("`%s`", matcher.group(0))
        );
        return out;
    }

    /**
     * Create a pattern instance from regex.
     * @param regex Regex
     * @return Patter
     * @throws IOException If fails
     */
    private static Pattern pattern(final String regex) throws IOException {
        return Pattern.compile(
            new JoinedText(
                "",
                "(?<= |^)",
                regex,
                "(?=(?:[^`]*`[^`]*`)*[^`]*$)"
            ).asString()
        );
    }

    /**
     * Find and replace.
     * @param txt The text
     * @param pattern The patter
     * @param replace The function to use for replacement
     * @return New text
     * @throws IOException If fails
     */
    private static String replace(final CharSequence txt, final Pattern pattern,
        final Func<Matcher, String> replace) throws IOException {
        final StringBuffer out = new StringBuffer(0);
        final Matcher matcher = pattern.matcher(txt);
        final IoCheckedFunc<Matcher, String> safe =
            new IoCheckedFunc<>(replace);
        while (matcher.find()) {
            final String rep = safe.apply(matcher).replace("$", "\\$");
            matcher.appendReplacement(out, rep);
        }
        matcher.appendTail(out);
        return out.toString();
    }

    /**
     * To plain text.
     */
    public static final class ToText {
        /**
         * The par.
         */
        private final String par;
        /**
         * Ctor.
         * @param txt The par
         */
        public ToText(final String txt) {
            this.par = txt;
        }
        @Override
        public String toString() {
            return this.par
                .replaceAll("\\[/z]\\([^)]+\\)", "")
                .replaceAll("\\[([^]]+)]\\([^)]+\\)", "$1")
                .replaceAll("`([^`]+)`", "$1");
        }
    }

    /**
     * To HTML.
     */
    public static final class ToHtml {
        /**
         * The par.
         */
        private final String par;
        /**
         * Ctor.
         * @param txt The par
         */
        public ToHtml(final String txt) {
            this.par = txt;
        }
        @Override
        public String toString() {
            return this.par
                .replaceAll("\\[(/z)]\\(([^)]+)\\)", "<a href='$2'>$1</a>")
                .replaceAll("\\[([^]]+)]\\(([^)]+)\\)", "<a href='$2'>$1</a>")
                .replaceAll("`([^`]+)`", "<code>$1</code>");
        }
    }
}
