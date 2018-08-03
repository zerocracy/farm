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

import com.jcabi.xml.XMLDocument;
import com.zerocracy.claims.ClaimsItem;
import com.zerocracy.entry.ClaimsOf;
import com.zerocracy.farm.fake.FkProject;
import com.zerocracy.farm.props.PropsFarm;
import java.util.Collection;
import org.cactoos.list.SolidList;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * Test case for {@link ClaimOnQuestion}.
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 * @checkstyle JavadocVariableCheck (500 lines)
 * @checkstyle VisibilityModifierCheck (500 lines)
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@RunWith(Parameterized.class)
public final class ClaimOnQuestionTest {

    @Parameterized.Parameter
    public String query;

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object> bundles() {
        return new SolidList<>(
            "role assign ARC yegor256",
            "just some text",
            "modifies_vacation_mode on"
        );
    }

    @Test
    public void buildsClaim() throws Exception {
        final Question question = new Question(
            new XMLDocument(
                this.getClass().getResource("q-project.xml")
            ),
            this.query
        );
        final FkProject project = new FkProject();
        new ClaimOnQuestion(question).claim()
            .postTo(new ClaimsOf(new PropsFarm(), project));
        final ClaimsItem claims = new ClaimsItem(project).bootstrap();
        MatcherAssert.assertThat(
            claims.iterate(),
            Matchers.iterableWithSize(1)
        );
    }

}
