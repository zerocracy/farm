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
package com.zerocracy.pmo;

import com.jcabi.dynamo.Attributes;
import com.jcabi.dynamo.QueryValve;
import com.jcabi.dynamo.Table;
import com.jcabi.xml.XMLDocument;
import com.zerocracy.Farm;
import com.zerocracy.Project;
import com.zerocracy.claims.ClaimIn;
import com.zerocracy.claims.ClaimOut;
import com.zerocracy.entry.ClaimsOf;
import com.zerocracy.entry.ExtDynamo;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.xembly.Xembler;

/**
 * Hints to publish.
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class Hint {

    /**
     * Farm.
     */
    private final Farm frm;

    /**
     * Rank.
     */
    private final int rank;

    /**
     * Age in seconds.
     */
    private final int age;

    /**
     * Claim to post.
     */
    private final ClaimOut claim;

    /**
     * Ctor.
     * @param farm The farm
     * @param seconds How long it should stay alive before the next hint
     * @param clm The claim
     * @checkstyle ParameterNumberCheck (5 lines)
     */
    public Hint(final Farm farm,
        final int seconds, final ClaimOut clm) {
        // @checkstyle MagicNumber (1 line)
        this(farm, 80, seconds, clm);
    }

    /**
     * Ctor.
     * @param farm The farm
     * @param clm The claim
     */
    public Hint(final Farm farm, final ClaimOut clm) {
        this(farm, (int) TimeUnit.DAYS.toSeconds(1L), clm);
    }

    /**
     * Ctor.
     * @param farm The farm
     * @param rnk Importance rank
     * @param seconds How long it should stay alive before the next hint
     * @param clm The claim
     * @checkstyle ParameterNumberCheck (5 lines)
     */
    public Hint(final Farm farm, final int rnk,
        final int seconds, final ClaimOut clm) {
        this.frm = farm;
        this.rank = rnk;
        this.age = seconds;
        this.claim = clm;
    }

    /**
     * Post it or ignore.
     * @param project The project to post to
     * @return TRUE if it was posted, FALSE if it was ignored
     * @throws IOException If fails
     */
    public boolean postTo(final Project project)
        throws IOException {
        final Table table = new ExtDynamo(this.frm).value()
            .table("0crat-hints");
        final String mnemo = this.mnemo(project);
        final boolean exists = !table.frame().through(
            new QueryValve().withLimit(1)
        ).where("mnemo", mnemo).isEmpty();
        final boolean posted;
        // @checkstyle MagicNumber (1 line)
        if (exists || this.rank < 50) {
            posted = false;
        } else {
            table.put(
                new Attributes()
                    .with("mnemo", mnemo)
                    .with(
                        "ttl",
                        (System.currentTimeMillis()
                            + TimeUnit.SECONDS.toMillis((long) this.age))
                            / TimeUnit.SECONDS.toMillis(1L)
                    )
                    .with("when", System.currentTimeMillis())
            );
            this.claim.postTo(new ClaimsOf(this.frm, project));
            posted = true;
        }
        return posted;
    }

    /**
     * Create its unique mnemo.
     * @param project The project to post to
     * @return Unique mnemo
     * @throws IOException If fails
     */
    private String mnemo(final Project project) throws IOException {
        final ClaimIn cin = new ClaimIn(
            new XMLDocument(
                new Xembler(this.claim).xmlQuietly()
            ).nodes("/claim").get(0)
        );
        return new StringBuilder(project.pid())
            .append(';').append(cin.token())
            .append(';').append(cin.param("mnemo"))
            .toString();
    }

}
