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
package com.zerocracy.pm;

import com.zerocracy.jstk.Item;
import com.zerocracy.jstk.Project;
import com.zerocracy.jstk.fake.FkProject;
import org.cactoos.io.LengthOf;
import org.cactoos.io.TeeInput;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link Claims}.
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.9
 * @checkstyle JavadocMethodCheck (500 lines)
 */
public final class ClaimsTest {

    @Test
    public void opensExistingClaimsXml() throws Exception {
        final Project project = new FkProject();
        try (final Item item = project.acq("claims.xml")) {
            new LengthOf(
                new TeeInput(
                    String.join(
                        " ",
                        "<claims",
                        "xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'",
                        // @checkstyle LineLength (1 line)
                        "xsi:noNamespaceSchemaLocation='https://raw.githubusercontent.com/zerocracy/datum/0.27/xsd/pm/claims.xsd'",
                        "version='0.1' updated='2017-03-27T11:18:09.228Z'/>"
                    ),
                    item.path()
                )
            ).value();
        }
        try (final Claims claims = new Claims(project)) {
            claims.add(new ClaimOut().token("test;test;1").type("just hello"));
            MatcherAssert.assertThat(
                claims.iterate().iterator().hasNext(),
                Matchers.is(true)
            );
        }
    }

    @Test
    public void handlesExceptionsCorrectly() throws Exception {
        final Project project = new FkProject();
        try (final Item item = project.acq("claims.xml")) {
            new LengthOf(
                new TeeInput(
                    String.join(
                        " ",
                        "<claims",
                        "xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'",
                        // @checkstyle LineLength (1 line)
                        "xsi:noNamespaceSchemaLocation='https://raw.githubusercontent.com/zerocracy/datum/0.27/xsd/pm/claims.xsd'",
                        "version='0.1' updated='2017-03-27T11:18:09.228Z'/>"
                    ),
                    item.path()
                )
            ).value();
        }
        try (final Claims claims = new Claims(project)) {
            claims.add(new ClaimOut().token("test;test;1").type("just hello"));
            MatcherAssert.assertThat(
                claims.iterate().iterator().hasNext(),
                Matchers.is(true)
            );
        }
    }

    @Test
    public void addsAndRemovesClaims() throws Exception {
        try (final Claims claims = new Claims(new FkProject())) {
            claims.add(new ClaimOut().token("test;test").type("hello"));
            MatcherAssert.assertThat(
                claims.iterate().iterator().next().xpath("token/text()").get(0),
                Matchers.startsWith("test;")
            );
        }
    }

}
