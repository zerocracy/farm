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
package com.zerocracy.tk.project;

import com.zerocracy.Farm;
import com.zerocracy.Project;
import com.zerocracy.claims.Footprint;
import com.zerocracy.tk.RsPage;
import com.zerocracy.tk.project.reports.AwardChampions;
import com.zerocracy.tk.project.reports.FtReport;
import com.zerocracy.tk.project.reports.OrderChampions;
import com.zerocracy.tk.project.reports.OrdersGivenByWeek;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Map;
import org.bson.Document;
import org.cactoos.list.SolidList;
import org.cactoos.map.MapEntry;
import org.cactoos.map.SolidMap;
import org.takes.Response;
import org.takes.facets.fork.RqRegex;
import org.takes.facets.fork.TkRegex;
import org.takes.rq.RqHref;
import org.takes.rs.xe.XeAppend;
import org.takes.rs.xe.XeChain;
import org.takes.rs.xe.XeDirectives;
import org.takes.rs.xe.XeTransform;
import org.xembly.Directives;

/**
 * Footprint report.
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@SuppressWarnings({ "PMD.AvoidDuplicateLiterals", "PMD.ExcessiveImports" })
public final class TkReport implements TkRegex {

    /**
     * Date format.
     */
    private static final DateTimeFormatter FMT =
        DateTimeFormatter.ISO_LOCAL_DATE.withZone(ZoneOffset.UTC);

    /**
     * Reports.
     * @checkstyle DiamondOperatorCheck (5 lines)
     */
    private static final Map<String, FtReport> REPORTS =
        new SolidMap<String, FtReport>(
            new MapEntry<>("order-champions", new OrderChampions()),
            new MapEntry<>("award-champions", new AwardChampions()),
            new MapEntry<>("orders-given-by-week", new OrdersGivenByWeek())
        );

    /**
     * Farm.
     */
    private final Farm farm;

    /**
     * Ctor.
     * @param frm Farm
     */
    public TkReport(final Farm frm) {
        this.farm = frm;
    }

    @Override
    public Response act(final RqRegex req) throws IOException {
        final RqHref.Smart href = new RqHref.Smart(req);
        final LocalDate start = LocalDate.parse(
            href.single(
                "start",
                TkReport.FMT.format(
                    ZonedDateTime.now().minus(1L, ChronoUnit.MONTHS)
                )
            ),
            TkReport.FMT
        );
        final LocalDate end = LocalDate.parse(
            href.single("end", TkReport.FMT.format(ZonedDateTime.now())),
            TkReport.FMT
        );
        final String report = href.single(
            "report", TkReport.REPORTS.entrySet().iterator().next().getKey()
        );
        return new RsPage(
            this.farm,
            "/xsl/report.xsl",
            req,
            () -> {
                final Project project = new RqProject(this.farm, req, "PO");
                final Collection<Document> docs;
                try (final Footprint footprint =
                    new Footprint(this.farm, project)) {
                    docs = new SolidList<>(
                        footprint.collection().aggregate(
                            TkReport.REPORTS.get(report).bson(
                                project,
                                start.atStartOfDay().atZone(
                                    ZoneOffset.UTC
                                ).toInstant(),
                                // @checkstyle MagicNumber (1 line)
                                end.atTime(23, 59).atZone(
                                    ZoneOffset.UTC
                                ).toInstant()
                            )
                        )
                    );
                    docs.size();
                }
                return new XeChain(
                    new XeAppend("project", project.pid()),
                    new XeAppend("report", report),
                    new XeAppend("title", TkReport.REPORTS.get(report).title()),
                    new XeAppend("start", TkReport.FMT.format(start)),
                    new XeAppend("end", TkReport.FMT.format(end)),
                    new XeAppend(
                        "reports",
                        new XeTransform<>(
                            TkReport.REPORTS.keySet(),
                            name -> new XeAppend("report", name)
                        )
                    ),
                    new XeAppend(
                        "rows",
                        new XeTransform<>(
                            docs,
                            doc -> new XeAppend(
                                "row",
                                new XeTransform<Map.Entry<String, Object>>(
                                    doc.entrySet(),
                                    ent -> new XeDirectives(
                                        new Directives()
                                            .add(ent.getKey())
                                            .attr(
                                                "type",
                                                ent.getValue()
                                                    .getClass()
                                                    .getName()
                                            )
                                            .set(ent.getValue())
                                    )
                                )
                            )
                        )
                    )
                );
            }
        );
    }

}
