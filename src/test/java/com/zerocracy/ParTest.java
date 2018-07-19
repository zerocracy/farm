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

import org.cactoos.text.JoinedText;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link Par}.
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class ParTest {

    @Test
    public void replacesKeyElements() throws Exception {
        MatcherAssert.assertThat(
            new Par(
                "@%s is a DEV",
                "in C63314D6Z",
                "and has %d job(s), as in §1",
                "see [$50](/p/C6331EE6Z?a=1)",
                "and gh:test2-0/test.-4#455",
                "and again %1$s"
            ).say("jack-me", 1),
            Matchers.equalTo(
                new JoinedText(
                    " ",
                    "@jack-me[/z](https://www.0crat.com/u/jack-me) is a `DEV`",
                    "in [C63314D6Z](https://www.0crat.com/p/C63314D6Z)",
                    "and has one job, as in",
                    "[§1](http://www.zerocracy.com/policy.html#1)",
                    "see [$50](https://www.0crat.com/p/C6331EE6Z?a=1)",
                    "and [#455](https://github.com/test2-0/test.-4/issues/455)",
                    "and again jack-me"
                ).asString()
            )
        );
    }

    @Test
    public void doesntTouchEmails() throws Exception {
        MatcherAssert.assertThat(
            new Par("Hey it's yegor256@gmail.com").say(),
            Matchers.containsString("yegor256@gmail.com")
        );
    }

    @Test
    public void turnsItIntoText() throws Exception {
        MatcherAssert.assertThat(
            new Par.ToText(new Par("`It` is @%s").say("dmarkov")).toString(),
            Matchers.equalTo("It is @dmarkov")
        );
    }

    @Test
    public void turnsItIntoHtml() throws Exception {
        MatcherAssert.assertThat(
            new Par.ToHtml(
                new Par("`Hey` you @%s").say("jeffy")
            ).toString(),
            Matchers.equalTo(
                // @checkstyle LineLength (1 line)
                "<code>Hey</code> you @jeffy<a href='https://www.0crat.com/u/jeffy'>/z</a>"
            )
        );
    }

    @Test
    public void ignoreCodeParts() throws Exception {
        final String text = "Hey, `my DEV friend`!";
        MatcherAssert.assertThat(
            new Par(text).say(),
            Matchers.equalTo(text)
        );
    }
}
