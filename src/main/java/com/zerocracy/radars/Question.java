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
package com.zerocracy.radars;

import com.jcabi.xml.StrictXML;
import com.jcabi.xml.XML;
import com.jcabi.xml.XSD;
import com.jcabi.xml.XSDDocument;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import org.cactoos.iterable.Mapped;
import org.cactoos.iterable.Sorted;
import org.cactoos.list.SolidList;

/**
 * Question in text.
 *
 * @since 1.0
 */
public final class Question {

    /**
     * Schema.
     */
    private static final XSD SCHEMA = XSDDocument.make(
        Question.class.getResource("question.xsd")
    );

    /**
     * XML config.
     */
    private final XML config;

    /**
     * Question text.
     */
    private final String text;

    /**
     * Code found.
     */
    private final AtomicReference<String> rcode;

    /**
     * Help found.
     */
    private final AtomicReference<String> rhelp;

    /**
     * Params found.
     */
    private final Map<String, String> rparams;

    /**
     * Must be invited?
     */
    private final AtomicBoolean rinvited;

    /**
     * Ctor.
     * @param xml XML config
     * @param txt Text
     */
    public Question(final XML xml, final String txt) {
        this.config = new StrictXML(xml, Question.SCHEMA);
        this.text = txt;
        this.rcode = new AtomicReference<>();
        this.rhelp = new AtomicReference<>();
        this.rparams = new HashMap<>(0);
        this.rinvited = new AtomicBoolean();
    }

    @Override
    public String toString() {
        return this.text;
    }

    /**
     * Does it match?
     * @return TRUE if the question was understood
     */
    public boolean matches() {
        this.start();
        return this.rcode.get() != null;
    }

    /**
     * Get the best help we can give at this moment.
     * @return Help text
     */
    public String help() {
        if (this.matches()) {
            throw new IllegalStateException(
                "The question matches, you can call help()"
            );
        }
        return this.rhelp.get();
    }

    /**
     * Matching code.
     * @return The code
     */
    public String code() {
        if (!this.matches()) {
            throw new IllegalStateException(
                "The question doesn't match, you can call code()"
            );
        }
        return this.rcode.get();
    }

    /**
     * Must be invited?
     * @return TRUE if the user must be invited
     */
    public boolean invited() {
        return this.matches() && this.rinvited.get();
    }

    /**
     * All matched parameters.
     * @return Map of params
     */
    public Map<String, String> params() {
        if (!this.matches()) {
            throw new IllegalStateException(
                "The question doesn't match, you can call params()"
            );
        }
        return this.rparams;
    }

    /**
     * Parse.
     */
    private void start() {
        this.rcode.set(null);
        this.rhelp.set("");
        this.parse(
            this.config.nodes("/question/cmd"),
            new ArrayList<>(Arrays.asList(this.text.trim().split("\\s+")))
        );
    }

    /**
     * Parse.
     * @param cmds Commands
     * @param parts All other parts left
     */
    private void parse(final Collection<XML> cmds, final List<String> parts) {
        final String part = parts.remove(0);
        boolean found = false;
        for (final XML cmd : cmds) {
            if (this.parse(cmd, part, parts)) {
                found = true;
                break;
            }
        }
        if (!found) {
            this.rhelp.set(
                String.format(
                    "Can't understand \"%s\", try one of these:\n  - %s",
                    part,
                    String.join(
                        "\n  - ",
                        new TreeSet<>(
                            new SolidList<CharSequence>(
                                new Mapped<>(
                                    cmd -> String.format(
                                        "`%s` %s",
                                        cmd.xpath("label/text()").get(0),
                                        cmd.xpath("help/text() ").get(0)
                                    ),
                                    cmds
                                )
                            )
                        )
                    )
                )
            );
        }
    }

    /**
     * Parse.
     * @param cmd Command
     * @param part Part
     * @param parts All other parts left
     * @return TRUE if parsed
     */
    private boolean parse(final XML cmd, final String part,
        final List<String> parts) {
        boolean matches = false;
        final Pattern pattern = Pattern.compile(
            cmd.xpath("regex/text() ").get(0),
            Pattern.CASE_INSENSITIVE
        );
        if (pattern.matcher(part).matches()) {
            this.rcode.set(cmd.xpath("code/text()").get(0));
            this.rinvited.set(
                Boolean.parseBoolean(
                    cmd.xpath("invited/text()").get(0)
                )
            );
            final Collection<XML> subs = cmd.nodes("cmds/cmd");
            if (subs.isEmpty()) {
                this.parseOpts(cmd, part, parts);
            } else if (!parts.isEmpty()) {
                this.parse(subs, parts);
            }
            matches = true;
        }
        return matches;
    }

    /**
     * Parse.
     * @param cmd Command
     * @param part Part
     * @param parts All other parts left
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private void parseOpts(final XML cmd, final String part,
        final List<String> parts) {
        final Collection<XML> opts = cmd.nodes("opts/opt");
        for (final XML opt : opts) {
            final String name = opt.xpath("name/text() ").get(0);
            if (parts.isEmpty() && !opt.nodes("optional").isEmpty()) {
                continue;
            }
            if (parts.isEmpty()) {
                this.rcode.set(null);
                this.rhelp.set(
                    String.format(
                        "Option `<%s>` is missing in `%s <%s>`:\n  %s",
                        name,
                        part,
                        String.join(
                            "> <",
                            new Mapped<>(
                                item -> item.xpath("name/text()  ").get(0),
                                opts
                            )
                        ),
                        String.join(
                            "\n  ",
                            new Sorted<String>(
                                new Mapped<XML, String>(
                                    item -> String.format(
                                        "\\* `<%s>`: %s",
                                        item.xpath("name/text()").get(0),
                                        item.xpath("help/text()").get(0)
                                    ),
                                    opts
                                )
                            )
                        )
                    )
                );
                break;
            }
            final String param = parts.remove(0);
            final Pattern regex = Pattern.compile(
                opt.xpath("regex/text()").get(0),
                Pattern.CASE_INSENSITIVE
            );
            if (regex.matcher(param).matches()) {
                this.rparams.put(name, param);
            } else {
                this.rcode.set(null);
                this.rhelp.set(
                    String.format(
                        "Argument \"%s\" doesn't match regex `%s`",
                        param, regex
                    )
                );
            }
        }
    }

}
