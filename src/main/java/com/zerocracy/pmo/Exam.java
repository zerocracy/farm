/**
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
package com.zerocracy.pmo;

import com.zerocracy.Farm;
import com.zerocracy.Par;
import com.zerocracy.Policy;
import com.zerocracy.SoftException;
import java.io.IOException;

/**
 * The exam we take for a person before allowing him to do some operation.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.22
 */
public final class Exam {

    /**
     * Farm.
     */
    private final Farm farm;

    /**
     * Login of the person.
     */
    private final String login;

    /**
     * Ctor.
     * @param frm The frm
     * @param user The user
     */
    public Exam(final Farm frm, final String user) {
        this.farm = frm;
        this.login = user;
    }

    /**
     * Make sure the user has this reputation minimum.
     * @param item Item in Policy
     * @param def The default
     * @throws IOException If failed
     */
    public void min(final String item, final int def) throws IOException {
        final int min = new Policy(this.farm).get(item, def);
        final int rank = new Awards(this.farm, this.login).bootstrap().total();
        if (rank < min) {
            final int par = Integer.parseInt(item.replaceAll("\\..+$", ""));
            throw new SoftException(
                new Par(
                    "Your reputation is %+d, while a minimum of %+d",
                    "is required in order to do this, see §%d"
                ).say(rank, min, par)
            );
        }
    }

}
