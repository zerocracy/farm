/*
 * Copyright (c) 2016-2019 Zerocracy
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
@SuppressWarnings("PMD.TooManyMethods")
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
    public Iterable<Errors.Error> iterate(final String system,
        final int limit,
        final long hours) {
        return new Mapped<>(
            Errors.Error::new,
            this.region
                .table(Errors.TABLE)
                .frame()
                .through(
                    new QueryValve().withLimit(limit)
                        .withAttributesToGet(
                            Errors.ATTR_LOCATION,
                            Errors.ATTR_SYSTEM,
                            Errors.ATTR_CREATED
                        )
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
     * Github error.
     */
    public static final class Error {

        /**
         * Error data.
         */
        private final Item item;

        /**
         * Ctor.
         * @param data Error data
         */
        public Error(final Item data) {
            this.item = data;
        }

        /**
         * Error location.
         * @return Coordinates location
         * @throws IOException If fails
         */
        public String location() throws IOException {
            return this.item.get(Errors.ATTR_LOCATION).getS();
        }

        /**
         * Error timestamp.
         * @return Epoch ms timestamp
         * @throws IOException If fails
         */
        public long timestamp() throws IOException {
            return Long.valueOf(this.item.get(Errors.ATTR_CREATED).getN());
        }

        /**
         * Error system.
         * @return System string
         * @throws IOException If fails
         */
        public String system() throws IOException {
            return this.item.get(Errors.ATTR_SYSTEM).getS();
        }
    }

    /**
     * Github error.
     */
    public static final class GhError {

        /**
         * Error.
         */
        private final Errors.Error err;

        /**
         * Github client.
         */
        private final com.jcabi.github.Github client;

        /**
         * Ctor.
         * @param err Error
         * @param client Github
         */
        GhError(final Errors.Error err,
            final com.jcabi.github.Github client) {
            this.err = err;
            this.client = client;
        }

        /**
         * Error.
         * @return Error
         */
        public Errors.Error error() {
            return this.err;
        }

        /**
         * Github comment.
         * @return Github comment
         * @throws IOException If fails
         */
        public Comment comment() throws IOException {
            final String[] parts = this.err.location().split("#");
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
        public Iterable<Errors.GhError> iterate(final int limit,
            final long hours) {
            return new Mapped<>(
                err -> new Errors.GhError(err, this.client),
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
        public void remove(final Errors.GhError comment) throws IOException {
            this.errors.remove(
                Errors.Github.SYSTEM, comment.error().timestamp()
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
