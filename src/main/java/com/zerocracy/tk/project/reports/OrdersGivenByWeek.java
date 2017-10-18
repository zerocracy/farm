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
package com.zerocracy.tk.project.reports;

import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Sorts;
import com.zerocracy.jstk.Project;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.bson.BsonDocument;
import org.bson.BsonString;
import org.bson.conversions.Bson;

/**
 * Orders given by weeks.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.18
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class OrdersGivenByWeek implements FtReport {

    @Override
    public List<? extends Bson> bson(final Project project,
        final Date start, final Date end) {
        return Arrays.asList(
            new Match(
                project, start, end,
                Filters.eq("type", "Order was given")
            ),
            Aggregates.group(
                new BsonDocument("$week", new BsonString("$created")),
                Accumulators.sum("total", 1)
            ),
            Aggregates.sort(Sorts.descending("_id")),
            Aggregates.project(
                Projections.fields(
                    Projections.exclude("_id"),
                    Projections.computed("week", "$_id"),
                    Projections.include("total")
                )
            )
        );
    }

    @Override
    public String title() {
        return String.join(
            " ",
            "This is a chronological list of weeks and the amount",
            "of orders that were given to project members",
            "during that time periods."
        );
    }

}
