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
package com.zerocracy.stk.pmo.profile.wallet;

import com.zerocracy.jstk.Project;
import com.zerocracy.jstk.Stakeholder;
import com.zerocracy.pm.Person;
import com.zerocracy.pmo.People;
import java.io.IOException;

/**
 * Set wallet of the user.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.9
 */
public final class StkSet implements Stakeholder {

    /**
     * Project.
     */
    private final Project project;

    /**
     * Tube.
     */
    private final Person person;

    /**
     * Bank.
     */
    private final String bank;

    /**
     * Wallet.
     */
    private final String wallet;

    /**
     * Ctor.
     * @param pkt Project
     * @param tbe Tube
     * @param bnk Bank
     * @param wlt Wallet
     * @checkstyle ParameterNumberCheck (5 lines)
     */
    public StkSet(final Project pkt, final Person tbe, final String bnk,
        final String wlt) {
        this.project = pkt;
        this.person = tbe;
        this.bank = bnk;
        this.wallet = wlt;
    }

    @Override
    public void work() throws IOException {
        new People(this.project).bootstrap();
        new People(this.project).wallet(
            this.person.uid(), this.bank, this.wallet
        );
        this.person.say(
            String.format(
                "Wallet of \"%s\" set to `%s:%s`",
                this.person.uid(),
                this.bank, this.wallet
            )
        );
    }
}
