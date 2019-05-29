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
package com.zerocracy.tk;

import com.zerocracy.Farm;
import com.zerocracy.pmo.People;
import com.zerocracy.pmo.Resume;
import com.zerocracy.pmo.Resumes;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.takes.Response;
import org.takes.facets.fork.RqRegex;
import org.takes.facets.fork.TkRegex;
import org.takes.rs.xe.XeAppend;
import org.takes.rs.xe.XeSource;

/**
 * Join Zerocracy form.
 *
 * @since 1.0
 *
 * @checkstyle JavadocMethodCheck (500 lines)
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class TkJoin implements TkRegex {

    /**
     * Farm.
     */
    private final Farm farm;

    /**
     * Constructor.
     * @param frm Farm
     */
    public TkJoin(final Farm frm) {
        this.farm = frm;
    }

    @Override
    public Response act(final RqRegex req) throws IOException {
        final String author = new RqUser(this.farm, req, false).value();
        final People people = new People(this.farm).bootstrap();
        people.touch(author);
        final Resumes resumes = new Resumes(this.farm).bootstrap();
        //@checkstyle FinalLocalVariableCheck (1 line)
        RsPage result;
        if (people.exists(author) && resumes.exists(author)) {
            final Resume resume = resumes.resume(author);
            final List<XeSource> items =  new ArrayList<>(
                Arrays.asList(
                    new XeAppend("login", resume.login()),
                    new XeAppend("text", resume.text()),
                    new XeAppend("personality", resume.personality()),
                    new XeAppend("soid", Long.toString(resume.soid())),
                    new XeAppend("telegram", resume.telegram())
                )
            );
            if (resumes.hasExaminer(resume.login())) {
                items.add(
                    new XeAppend(
                        "examiner",
                        resumes.examiner(resume.login())
                    )
                );
            }
            result = new RsPage(
                this.farm,
                "/xsl/resume.xsl",
                req,
                () -> new XeAppend(
                "resume",
                    items
                )
            );
        } else {
            result =  new RsPage(this.farm, "/xsl/join.xsl", req);
        }
        return result;
    }
}
