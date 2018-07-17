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
package com.zerocracy.farm;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.jcabi.dynamo.Attributes;
import com.jcabi.dynamo.Conditions;
import com.jcabi.dynamo.Item;
import com.jcabi.dynamo.QueryValve;
import com.jcabi.dynamo.Region;
import com.jcabi.github.Comment;
import com.jcabi.github.Coordinates;
import com.jcabi.github.safe.SfComment;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import org.cactoos.collection.Mapped;

/**
 * Github error comments stored in DynamoDB.
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class Errors {
    /**
     * DynamoDB table.
     */
    private static final String TABLE = "0crat-errors";
    /**
     * Comment location.
     */
    private static final String ATTR_LOCATION = "location";
    /**
     * Created timestamp attribute.
     */
    private static final String ATTR_CREATED = "created";
    /**
     * Comment system, e.g. 'github'.
     */
    private static final String ATTR_SYSTEM = "system";

    /**
     * DynamoDB region.
     */
    private final Region region;

    /**
     * Ctor.
     * @param rgn DynamoDB region
     */
    public Errors(final Region rgn) {
        this.region = rgn;
    }

    /**
     * Add a comment.
     * @param system Comment system
     * @param location Comment location
     * @param timestamp Created timestamp
     * @throws IOException If fails
     */
    public void add(final String system, final String location,
        final long timestamp) throws IOException {
        this.region
            .table(Errors.TABLE)
            .put(
                new Attributes()
                    .with(Errors.ATTR_SYSTEM, system)
                    .with(Errors.ATTR_LOCATION, location)
                    .with(Errors.ATTR_CREATED, timestamp)
            );
    }

    /**
     * Iterate comments.
     * @param system Comment system, e.g. 'github'
     * @param limit Result limit
     * @param hours Hours filter
     * @return Github comments with errors
     */
    public Iterable<String> iterate(final String system,
        final int limit,
        final long hours) {
        return new Mapped<>(
            (Item item) -> item.get(Errors.ATTR_LOCATION).getS(),
            this.region
                .table(Errors.TABLE)
                .frame()
                .through(
                    new QueryValve().withLimit(limit)
                        .withAttributeToGet(Errors.ATTR_LOCATION)
                )
                .where(Errors.ATTR_SYSTEM, Conditions.equalTo(system))
                .where(
                    Errors.ATTR_CREATED,
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
     * @param system Comment system
     * @param timestamp Comment timestamp
     * @throws IOException If fails
     */
    public void remove(final String system, final long timestamp)
        throws IOException {
        this.region
            .table(Errors.TABLE)
            .delete(
                new Attributes()
                    .with(Errors.ATTR_SYSTEM, system)
                    .with(Errors.ATTR_CREATED, timestamp)
            );
    }

    /**
     * Github comment system wrapper.
     */
    public static final class Github {
        /**
         * Comment system.
         */
        private static final String SYSTEM = "github";

        /**
         * Errors table.
         */
        private final Errors errors;
        /**
         * Github client.
         */
        private final com.jcabi.github.Github client;

        /**
         * Ctor.
         * @param errors Errors table
         * @param github Github client
         */
        public Github(final Errors errors,
            final com.jcabi.github.Github github) {
            this.errors = errors;
            this.client = github;
        }

        /**
         * Iterate Github comments.
         * @param limit Comments limit
         * @param hours Time frame
         * @return Github comments
         */
        public Iterable<Comment> iterate(final int limit, final long hours) {
            return new Mapped<>(
                this::comment,
                this.errors.iterate(Errors.Github.SYSTEM, limit, hours)
            );
        }

        /**
         * Add Github comment.
         * @param comment A comment
         * @throws IOException If fails
         */
        public void add(final Comment comment) throws IOException {
            this.errors.add(
                Errors.Github.SYSTEM,
                Errors.Github.location(comment),
                new Comment.Smart(comment).createdAt().getTime()
            );
        }

        /**
         * Remove Github comment.
         * @param comment A comment
         * @throws IOException If fails
         */
        public void remove(final Comment comment) throws IOException {
            this.errors.remove(
                Errors.Github.SYSTEM,
                new Comment.Smart(comment).createdAt().getTime()
            );
        }

        /**
         * Make a Github comment from location.
         * @param location Comment location
         * @return Github comment
         */
        private Comment comment(final String location) {
            final String[] parts = location.split("#");
            return new SfComment(
                this.client
                    .repos()
                    .get(new Coordinates.Simple(parts[0]))
                    .issues()
                    .get(Integer.valueOf(parts[1]))
                    .comments()
                    .get(Integer.valueOf(parts[2]))
            );
        }

        /**
         * Make comment location.
         * @param comment Github comment
         * @return Location string
         */
        private static String location(final Comment comment) {
            return String.format(
                "%s#%d#%d",
                comment.issue().repo()
                    .coordinates(),
                comment.issue().number(),
                comment.number()
            );
        }
    }
}
