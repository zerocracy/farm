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
package com.zerocracy.pm.staff.voters;

import com.zerocracy.jstk.Project;
import com.zerocracy.pm.staff.Voter;
import com.zerocracy.pmo.People;
import java.io.IOException;

/**
 * Vacation voter.
 * @author Kirill (g4s8.public@gmail.com)
 * @version $Id$
 * @since 0.16
 * @todo #110:30min Add a stakeholder to change person's
 *  vacation status and two commands in `q-profile`:
 *  "vacation on" and "vacation off". Stakeholder should
 *  change `vacation` flag in people.xml.
 */
public final class Vacation implements Voter {

    /**
     * A project.
     */
    private final People people;

    /**
     * Ctor.
     * @param project A project
     */
    public Vacation(final Project project) {
        this(new People(project));
    }

    /**
     * Primary ctor.
     * @param people People
     */
    private Vacation(final People people) {
        this.people = people;
    }

    @Override
    public double vote(
        final String login,
        final StringBuilder log
    ) throws IOException {
        final double vote;
        if (this.people.bootstrap().vacation(login)) {
            log.append("On vacation");
            vote = 1.0D;
        } else {
            vote = 0.0D;
        }
        return vote;
    }
}
