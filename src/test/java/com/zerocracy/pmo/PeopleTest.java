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
package com.zerocracy.pmo;

import com.zerocracy.jstk.cash.Cash;
import com.zerocracy.jstk.fake.FkProject;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link People}.
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.1
 */
public final class PeopleTest {

    /**
     * Adds and finds people.
     * @throws Exception If some problem inside
     */
    @Test
    public void addsAndFindsPeople() throws Exception {
        final People people = new People(new FkProject());
        people.bootstrap();
        final String uid = "U67WE3343P";
        final String rel = "github";
        final String alias = "yegor256";
        people.link(uid, rel, alias);
        people.link(uid, "jira", "http://www.0crat.com/jira");
        MatcherAssert.assertThat(
            people.find(rel, alias),
            Matchers.not(Matchers.emptyIterable())
        );
        MatcherAssert.assertThat(
            people.links(uid),
            Matchers.hasItem("github:yegor256")
        );
    }

    /**
     * Set rate of the user.
     * @throws Exception If some problem inside
     */
    @Test
    public void setsUserRate() throws Exception {
        final People people = new People(new FkProject());
        people.bootstrap();
        final String uid = "U67ZP3343P";
        people.rate(uid, new Cash.S("$35"));
        people.rate(uid, new Cash.S("$50"));
        MatcherAssert.assertThat(
            people.rate(uid),
            Matchers.equalTo(new Cash.S("USD 50"))
        );
    }

    /**
     * Adds and finds user skills.
     * @throws Exception If some problem inside
     */
    @Test
    public void setsAndFetchesUserSkills() throws Exception {
        final People people = new People(new FkProject());
        people.bootstrap();
        final String uid = "U67FE3Z43P";
        final String skill = "java";
        people.skill(uid, skill);
        people.skill(uid, "java.spring");
        people.skill(uid, "ruby");
        MatcherAssert.assertThat(
            people.skills(uid),
            Matchers.hasItem(skill)
        );
    }

}
