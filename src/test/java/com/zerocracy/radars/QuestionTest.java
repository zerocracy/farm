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

import com.jcabi.xml.XML;
import com.jcabi.xml.XMLDocument;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link Question}.
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class QuestionTest {

    @Test
    public void parsesValidText() throws Exception {
        final Question question = new Question(
            new XMLDocument(
                this.getClass().getResource("q-project.xml")
            ),
            "assign ARC yegor256 $40"
        );
        MatcherAssert.assertThat(
            question.matches(),
            Matchers.equalTo(true)
        );
        MatcherAssert.assertThat(
            question.code(),
            Matchers.equalTo("Assign role")
        );
        MatcherAssert.assertThat(
            question.params().get("role"),
            Matchers.equalTo("ARC")
        );
        MatcherAssert.assertThat(
            question.params().get("rate"),
            Matchers.equalTo("$40")
        );
    }

    @Test
    public void buildsHelp() throws Exception {
        final Question question = new Question(
            new XMLDocument(
                this.getClass().getResource("q-profile.xml")
            ),
            "breakup"
        );
        MatcherAssert.assertThat(
            question.matches(),
            Matchers.equalTo(false)
        );
        MatcherAssert.assertThat(
            question.help(),
            Matchers.containsString(
                "Option `<login>` is missing in `breakup <login>`"
            )
        );
    }

    @Test
    public void parsesWithoutOptionalArgument() throws Exception {
        final Question question = new Question(
            new XMLDocument(
                this.getClass().getResource("q-profile.xml")
            ),
            "rate"
        );
        MatcherAssert.assertThat(
            question.matches(),
            Matchers.equalTo(true)
        );
        MatcherAssert.assertThat(
            question.invited(),
            Matchers.equalTo(true)
        );
    }

    @Test
    public void buildsHelpOnInvalidCommand() throws Exception {
        final Question question = new Question(
            new XMLDocument(
                this.getClass().getResource("q-tracker.xml")
            ),
            "just-a-weird-command"
        );
        MatcherAssert.assertThat(
            question.matches(),
            Matchers.equalTo(false)
        );
        MatcherAssert.assertThat(
            question.help(),
            Matchers.containsString("try one of these")
        );
    }

    // @checkstyle NestedForDepthCheck (150 lines)
    @Test
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public void parsesManyValidTexts() throws Exception {
        final String[] files = {
            "q-profile-test.xml",
            "q-project-test.xml",
            "q-tracker-test.xml",
        };
        for (final String file : files) {
            final XML test = new XMLDocument(
                this.getClass().getResource(file)
            );
            final XML target = new XMLDocument(
                this.getClass().getResource(test.xpath("/test/@target").get(0))
            );
            for (final XML cmd : test.nodes("/test/cmd")) {
                final Question question = new Question(
                    target, cmd.xpath("text/text()").get(0)
                );
                MatcherAssert.assertThat(
                    String.format("Question '%s' doesn't match", question),
                    question.matches(),
                    Matchers.equalTo(true)
                );
                MatcherAssert.assertThat(
                    question.code(),
                    Matchers.equalTo(cmd.xpath("code/text()").get(0))
                );
                for (final XML opt : cmd.nodes("opts/opt")) {
                    MatcherAssert.assertThat(
                        question.params(),
                        Matchers.hasEntry(
                            Matchers.equalTo(opt.xpath("name/text()").get(0)),
                            Matchers.equalTo(opt.xpath("value/text()").get(0))
                        )
                    );
                }
            }
        }
    }

}
