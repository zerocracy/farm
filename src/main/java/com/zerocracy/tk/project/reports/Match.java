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
package com.zerocracy.tk.project.reports;

import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.zerocracy.Project;
import java.io.IOException;
import java.time.Instant;
import org.bson.BsonDocument;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;
import org.cactoos.iterable.Joined;
import org.cactoos.list.SolidList;

/**
 * Match.
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
final class Match implements Bson {

    /**
     * Project.
     */
    private final Project project;

    /**
     * Start.
     */
    private final Instant start;

    /**
     * End.
     */
    private final Instant end;

    /**
     * Terms.
     */
    private final Iterable<Bson> terms;

    /**
     * Ctor.
     * @param pkt The project
     * @param left Start
     * @param right End
     * @param items Extra terms
     * @checkstyle ParameterNumberCheck (5 lines)
     */
    Match(final Project pkt, final Instant left, final Instant right,
        final Bson... items) {
        this(pkt, left, right, new SolidList<>(items));
    }

    /**
     * Ctor.
     * @param pkt The project
     * @param left Start
     * @param right End
     * @param items Extra terms
     * @checkstyle ParameterNumberCheck (5 lines)
     */
    Match(final Project pkt, final Instant left, final Instant right,
        final Iterable<Bson> items) {
        this.project = pkt;
        this.start = left;
        this.end = right;
        this.terms = items;
    }

    @Override
    public <T> BsonDocument toBsonDocument(final Class<T> type,
        final CodecRegistry reg) {
        try {
            return Aggregates.match(
                Filters.and(
                    new Joined<Bson>(
                        new SolidList<>(
                            Filters.eq("project", this.project.pid()),
                            Filters.gt("created", this.start),
                            Filters.lt("created", this.end)
                        ),
                        this.terms
                    )
                )
            ).toBsonDocument(type, reg);
        } catch (final IOException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
