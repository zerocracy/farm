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
package com.zerocracy.pmo.profile;

import com.zerocracy.jstk.Project;
import com.zerocracy.jstk.Stakeholder;
import com.zerocracy.jstk.cash.Cash;
import com.zerocracy.pm.Person;
import com.zerocracy.pmo.People;
import java.io.IOException;

/**
 * Set rate of the user.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.1
 */
public final class Rate implements Stakeholder {

    /**
     * Project.
     */
    private final Project project;

    /**
     * Tube.
     */
    private final Person person;

    /**
     * Rate.
     */
    private final Cash money;

    /**
     * Ctor.
     * @param pkt Project
     * @param tbe Tube
     * @param cash Cash value
     */
    public Rate(final Project pkt, final Person tbe, final Cash cash) {
        this.project = pkt;
        this.person = tbe;
        this.money = cash;
    }

    @Override
    public void work() throws IOException {
        new People(this.project).bootstrap();
        new People(this.project).rate(this.person.uid(), this.money);
        this.person.say(
            String.format(
                "Rate of \"%s\" set to %s",
                this.person.uid(),
                this.money
            )
        );
    }
}
