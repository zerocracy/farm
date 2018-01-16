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
package com.zerocracy.farm;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.jcabi.dynamo.Attributes;
import com.jcabi.dynamo.Item;
import com.jcabi.dynamo.Region;
import com.jcabi.dynamo.ScanValve;
import com.jcabi.github.Comment;
import com.jcabi.github.Coordinates;
import com.jcabi.github.Github;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import org.cactoos.collection.Mapped;

/**
 * Github error comments stored in DynamoDB.
 *
 * @author Kirill (g4s8.public@gmail.com)
 * @version $Id$
 * @since 0.20
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class DyErrors {
    /**
     * DynamoDB table.
     */
    private static final String TABLE = "0crat-errors";
    /**
     * Comment id attribute.
     */
    private static final String ATTR_COMMENT = "comment";
    /**
     * Created timestamp attribute.
     */
    private static final String ATTR_CREATED = "created";

    /**
     * DynamoDB region.
     */
    private final Region region;

    /**
     * Ctor.
     * @param region DynamoDB region
     */
    public DyErrors(final Region region) {
        this.region = region;
    }

    /**
     * Add a comment.
     * @param comment Github comment
     * @throws IOException If fails
     */
    public void add(final Comment comment) throws IOException {
        this.region
            .table(DyErrors.TABLE)
            .put(
                new Attributes()
                    .with(DyErrors.ATTR_COMMENT, DyErrors.hash(comment))
                    .with(
                        DyErrors.ATTR_CREATED,
                        new Comment.Smart(comment).createdAt().getTime()
                    )
            );
    }

    /**
     * Iterate comments.
     * @param github Github client
     * @param limit Result limit
     * @param hours Hours filter
     * @return Github comments with errors
     */
    public Iterable<Comment> iterate(final Github github,
        final int limit,
        final long hours) {
        return new Mapped<>(
            (Item item) -> DyErrors.comment(github, item),
            this.region
                .table(DyErrors.TABLE)
                .frame()
                .through(new ScanValve().withLimit(limit))
                .where(
                    DyErrors.ATTR_CREATED,
                    new Condition()
                        .withComparisonOperator(ComparisonOperator.LT)
                        .withAttributeValueList(
                            new AttributeValue()
                                .withN(
                                    Long.toString(
                                        new Date().getTime()
                                            - TimeUnit.HOURS.toMillis(hours)
                                    )
                                )
                        )
                )
        );
    }

    /**
     * Remove a comment from table.
     * @param comment Comment to remove
     * @throws IOException If fails
     */
    public void remove(final Comment comment) throws IOException {
        this.region
            .table(DyErrors.TABLE)
            .delete(
                new Attributes()
                    .with(DyErrors.ATTR_COMMENT, DyErrors.hash(comment))
            );
    }

    /**
     * Make a Github comment from DynamoDB item.
     * @param github Github client
     * @param item DynamoDB item
     * @return Github comment
     * @throws IOException If fails
     */
    private static Comment comment(final Github github, final Item item)
        throws IOException {
        final String comment = item.get(DyErrors.ATTR_COMMENT).getS();
        final String[] parts = comment.split("#");
        return github
            .repos()
            .get(new Coordinates.Simple(parts[0]))
            .issues()
            .get(Integer.valueOf(parts[1]))
            .comments()
            .get(Integer.valueOf(parts[2]));
    }

    /**
     * Hash key for comment.
     * @param comment A comment
     * @return Hash string
     */
    private static String hash(final Comment comment) {
        return String.format(
            "%s#%d#%d",
            comment.issue().repo()
                .coordinates(),
            comment.issue().number(),
            comment.number()
        );
    }
}
