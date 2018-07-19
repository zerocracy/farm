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
package com.zerocracy.tk;

import com.jcabi.xml.XML;
import com.zerocracy.Farm;
import com.zerocracy.pmo.Catalog;
import com.zerocracy.pmo.Vacancies;
import java.io.IOException;
import org.cactoos.iterable.Mapped;
import org.takes.Request;
import org.takes.Response;
import org.takes.Take;
import org.takes.rs.xe.XeAppend;
import org.takes.rs.xe.XeChain;

/**
 * Vacancies of all projects.
 *
 * @since 1.0
 */
public final class TkVacancies implements Take {

    /**
     * Farm.
     */
    private final Farm frm;

    /**
     * Ctor.
     *
     * @param farm Farm
     */
    public TkVacancies(final Farm farm) {
        this.frm = farm;
    }

    @Override
    public Response act(final Request req) throws IOException {
        final Vacancies vacancies = new Vacancies(this.frm).bootstrap();
        final Catalog catalog = new Catalog(this.frm).bootstrap();
        return new RsPage(
            this.frm,
            "/xsl/vacancies.xsl",
            req,
            () -> new XeAppend(
                "vacancies",
                new XeChain(
                    new Mapped<>(
                        pid -> {
                            final XML vacancy = vacancies.vacancy(pid);
                            return new XeAppend(
                                "vacancy",
                                new XeAppend("project", pid),
                                new XeAppend("name", catalog.title(pid)),
                                new XeAppend(
                                    "text",
                                    vacancy.xpath("text/text()").get(0)
                                ),
                                new XeAppend(
                                    "added",
                                    vacancy.xpath("added/text()").get(0)
                                ),
                                new XeAppend(
                                    "author",
                                    vacancy.xpath("author/text()").get(0)
                                )
                            );
                        },
                        vacancies.iterate()
                    )
                )
            )
        );
    }
}
