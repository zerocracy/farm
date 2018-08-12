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
package com.zerocracy.pm.staff;

import com.jcabi.xml.XML;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.cactoos.collection.Filtered;
import org.cactoos.collection.Sorted;
import org.cactoos.iterable.ItemAt;
import org.cactoos.list.Joined;
import org.cactoos.list.Mapped;
import org.cactoos.map.MapOf;
import org.cactoos.scalar.IoCheckedScalar;
import org.cactoos.scalar.MaxOf;
import org.cactoos.scalar.Or;
import org.cactoos.scalar.StickyScalar;
import org.cactoos.scalar.SumOf;

/**
 * Result of {@link Elections}.
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class ElectionResult {

    /**
     * Election XML.
     */
    private final List<XML> elections;

    /**
     * Table (votes by user).
     */
    private final IoCheckedScalar<Map<String, List<Vote>>> table;

    /**
     * Ctor.
     *
     * @param elections XML
     */
    public ElectionResult(final List<XML> elections) {
        this.elections = elections;
        this.table = new IoCheckedScalar<>(
            new StickyScalar<>(
                () -> ElectionResult.asTable(
                    elections.get(0).nodes("//election").get(0)
                )
            )
        );
    }

    /**
     * Election winner.
     *
     * @return Winner's login
     * @throws IOException If fails
     */
    public String winner() throws IOException {
        return new IoCheckedScalar<>(
            new ItemAt<>(
                new Sorted<>(
                    (left, right) -> Double.compare(
                        right.getValue(), left.getValue()
                    ),
                    new Filtered<>(
                        score -> !score.getValue().isNaN()
                            && score.getValue().compareTo(0.0) > 0,
                        new MapOf<>(
                            Map.Entry::getKey,
                            entry -> new SumOf(
                                new Mapped<>(Vote::score, entry.getValue())
                            ).value(),
                            this.table.value().entrySet()
                        ).entrySet()
                    )
                )
            )
        ).value().getKey();
    }

    /**
     * Election reason.
     *
     * @return Reason text
     * @throws IOException If fails
     */
    public String reason() throws IOException {
        final int max = ElectionResult.max(
            this.elections.get(0).nodes("//election").get(0)
        );
        return String.join(
            "\n",
            new Mapped<>(
                entry -> String.format(
                    "@%s (%.2f of %d):\n%s",
                    entry.getKey(),
                    new SumOf(
                        new Mapped<>(
                            vote -> vote.score(),
                            entry.getValue()
                        )
                    ).doubleValue(),
                    max,
                    String.join(
                        "\n",
                        new Mapped<>(
                            vote -> String.format("  %s", vote),
                            entry.getValue()
                        )
                    )
                ),
                this.table.value().entrySet()
            )
        );
    }

    /**
     * Check if elected.
     *
     * @return TRUE if elected
     * @throws IOException If fails
     */
    public boolean elected() throws IOException {
        return !this.elections.isEmpty()
            && new IoCheckedScalar<>(
            new Or(
                (Number sum) -> {
                    final Double dbl = sum.doubleValue();
                    return !dbl.isNaN() && dbl.compareTo(0.0) > 0;
                },
                new Mapped<>(
                    votes -> new SumOf(new Mapped<>(Vote::score, votes)),
                    this.table.value().values()
                )
            )
        ).value();
    }

    /**
     * View election votes as table.
     *
     * @param election Election XML
     * @return Table map
     */
    private static Map<String, List<Vote>> asTable(final XML election) {
        return new MapOf<>(
            login -> login,
            login -> new Mapped<>(
                vote -> new Vote(
                    Integer.valueOf(vote.xpath("@weight").get(0)),
                    Double.valueOf(
                        vote.xpath(
                            String.format(
                                "person[@login='%s']/@points",
                                login
                            )
                        ).get(0)
                    ),
                    new ItemAt<>(
                        "",
                        vote.xpath(
                            String.format(
                                "person[@login='%s']/text()",
                                login
                            )
                        )
                    ).value()
                ),
                election.nodes("vote")
            ),
            new HashSet<>(election.xpath("vote/person/@login"))
        );
    }

    /**
     * Max score of elections.
     *
     * @param election Election XML
     * @return Max score
     */
    private static int max(final XML election) {
        return new MaxOf(
            new Mapped<String, Number>(
                login -> new SumOf(
                    new Filtered<Number>(
                        weight -> weight.intValue() > 0,
                        new Joined<Number>(
                            new Mapped<XML, Number>(
                                (XML vote) -> Integer.valueOf(
                                    vote.xpath("@weight").get(0)
                                ),
                                election.nodes("vote")
                            )
                        )
                    )
                ).value(),
                new HashSet<>(election.xpath("vote/person/@login"))
            )
        ).intValue();
    }

    /**
     * One election vote.
     */
    private static final class Vote {

        /**
         * Weight.
         */
        private final int weight;
        /**
         * Points.
         */
        private final double points;
        /**
         * Details text.
         */
        private final String details;

        /**
         * Ctor.
         *
         * @param weight Weight
         * @param points Points
         * @param details Details
         */
        Vote(final int weight, final double points,
            final String details) {
            this.weight = weight;
            this.points = points;
            this.details = details;
        }

        /**
         * Vote's score.
         *
         * @return Score of this vote
         */
        public double score() {
            return this.weight * this.points;
        }

        @Override
        public String toString() {
            return String.format(
                "+%.2f=%.2fx%d %s",
                this.score(), this.points, this.weight, this.details
            );
        }
    }
}
