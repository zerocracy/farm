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
package com.zerocracy.pm.qa;

import com.jcabi.github.Comment;
import com.jcabi.github.Issue;
import com.zerocracy.Farm;
import com.zerocracy.Project;
import com.zerocracy.entry.ExtGithub;
import com.zerocracy.pm.in.Orders;
import com.zerocracy.pm.staff.Roles;
import com.zerocracy.radars.github.Job;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import org.cactoos.collection.Mapped;
import org.cactoos.list.ListOf;
import org.cactoos.scalar.IoCheckedScalar;
import org.cactoos.scalar.Or;

/**
 * GitHub job audit.
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@SuppressWarnings("PMD.TooManyMethods")
public final class JobAudit {

    /**
     * Farm.
     */
    private final Farm farm;

    /**
     * Project.
     */
    private final Project project;

    /**
     * Ctor.
     * @param frm The farm
     * @param pkt Project
     */
    public JobAudit(final Farm frm, final Project pkt) {
        this.farm = frm;
        this.project = pkt;
    }

    /**
     * Review the job.
     * @param job The job to add
     * @return List of complains
     * @throws IOException If fails
     */
    public Collection<String> review(final String job) throws IOException {
        final Roles roles = new Roles(this.project).bootstrap();
        final Orders orders = new Orders(this.farm, this.project).bootstrap();
        final List<String> arcs = roles.findByRole("ARC");
        final String performer = orders.performer(job);
        final Issue.Smart issue = new Issue.Smart(
            new Job.Issue(new ExtGithub(this.farm).value(), job)
        );
        final Collection<String> complaints = new LinkedList<>();
        if (issue.isPull()) {
            final List<String> authors = new ListOf<>(
                new Mapped<>(
                    cmt -> new Comment.Smart(cmt).author().login(),
                    issue.comments().iterate(new Date(0L))
                )
            );
            final boolean seen = new IoCheckedScalar<>(
                new Or(arcs::contains, authors)
            ).value();
            if (!seen || !authors.contains(performer)) {
                complaints.add("");
            }
        }
        return complaints;
    }

}
