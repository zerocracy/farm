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
package com.zerocracy.stk.pmo.profile.skills;

import com.jcabi.aspects.Tv;
import com.jcabi.xml.XML;
import com.zerocracy.jstk.Project;
import com.zerocracy.jstk.Stakeholder;
import com.zerocracy.pm.ClaimIn;
import com.zerocracy.pmo.People;
import com.zerocracy.stk.SoftException;
import java.io.IOException;
import java.util.Collection;

/**
 * Add new skill to the user.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id: 3b928d219d27bc96beb0ec3b3bbc5c60aa9d8060 $
 * @since 0.1
 */
public final class StkAdd implements Stakeholder {

    @Override
    public void process(final Project pmo,
        final XML xml) throws IOException {
        final People people = new People(pmo).bootstrap();
        final ClaimIn claim = new ClaimIn(xml);
        final String login = claim.param("person");
        final Collection<String> skills = people.skills(login);
        if (skills.size() > Tv.FIVE) {
            throw new SoftException(
                String.format(
                    "You've got too many skills already: `%s` (max is five).",
                    String.join("`, `", skills)
                )
            );
        }
        final String skill = claim.param("skill");
        people.skill(login, skill);
        claim.reply(
            String.format(
                "New skill \"%s\" added to \"%s\".",
                skill,
                login
            )
        ).postTo(pmo);
    }

}
