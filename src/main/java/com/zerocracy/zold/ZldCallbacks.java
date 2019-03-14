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
package com.zerocracy.zold;

import com.jcabi.jdbc.JdbcSession;
import com.jcabi.jdbc.Outcome;
import com.jcabi.jdbc.SingleOutcome;
import com.zerocracy.Farm;
import com.zerocracy.Project;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Iterator;
import javax.sql.DataSource;

/**
 * Zold callbacks.
 *
 * @since 1.0
 * @checkstyle ParameterNumberCheck (500 lines)
 */
@SuppressWarnings({"PMD.UseObjectForClearerAPI", "PMD.AvoidDuplicateLiterals"})
public final class ZldCallbacks {

    /**
     * Database.
     */
    private final DataSource data;

    /**
     * Farm.
     */
    private final Farm farm;

    /**
     * Ctor.
     * @param data Database
     * @param farm Farm
     */
    public ZldCallbacks(final DataSource data, final Farm farm) {
        this.data = data;
        this.farm = farm;
    }

    /**
     * Add new callback.
     * @param project PRoject id
     * @param callback Callback id
     * @param secret Secret string
     * @param token Token
     * @param prefix Invoice prefix
     * @throws IOException If fails
     */
    public void add(final String project, final String callback,
        final String secret, final String token, final String prefix
    ) throws IOException {
        try {
            new JdbcSession(this.data)
                .sql(
                    String.join(
                        " ",
                        "INSERT INTO zold_invoices",
                        "(callback, prefix, project, secret, token)",
                        "VALUES",
                        "(?, ?, ?, ?, ?)"
                    )
                )
                .set(callback)
                .set(prefix)
                .set(project)
                .set(secret)
                .set(token)
                .insert(Outcome.VOID);
        } catch (final SQLException err) {
            throw new IOException("Failde to add callback", err);
        }
    }

    /**
     * Take project by callback and invalidate callback.
     * @param callback Callback id
     * @param code Secret code
     * @param secret Token
     * @param prefix Invoice prefix
     * @return Project if found
     * @throws IOException If fails
     */
    public Project take(final String callback, final String code,
        final String secret, final String prefix) throws IOException {
        try {
            final JdbcSession session = new JdbcSession(this.data)
                .autocommit(false);
            final Iterator<Project> iter = this.farm.find(
                String.format(
                    "@id='%s'",
                    session
                        .sql(
                            String.join(
                                " ",
                                "SELECT project FROM zold_invoices",
                                "WHERE callback = ? AND prefix = ?",
                                "AND secret = ? AND token = ?"
                            )
                        ).set(callback).set(prefix)
                        .set(code).set(secret)
                        .select(new SingleOutcome<>(String.class))
                )
            ).iterator();
            if (!iter.hasNext()) {
                throw new IOException("Project for callback doesn't exist");
            }
            session.sql(
                String.join(
                    " ",
                    "UPDATE zold_invoices SET token = ?",
                    "WHERE callback = ? AND prefix = ?",
                    "AND secret = ? AND token = ?"
                )
            ).set(String.format("x:%s", secret))
                .set(callback).set(prefix)
                .set(code).set(secret)
                .execute()
                .commit();
            return iter.next();
        } catch (final SQLException err) {
            throw new IOException("Failed to find project", err);
        }
    }
}
