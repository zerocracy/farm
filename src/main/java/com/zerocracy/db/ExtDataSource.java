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
package com.zerocracy.db;

import com.jcabi.aspects.Tv;
import com.zerocracy.Farm;
import com.zerocracy.farm.props.Props;
import java.io.IOException;
import javax.sql.DataSource;
import org.apache.commons.dbcp.BasicDataSource;
import org.cactoos.Scalar;
import org.cactoos.func.IoCheckedFunc;
import org.cactoos.func.SolidFunc;

/**
 * Data source.
 *
 * @since 1.0
 */
public final class ExtDataSource implements Scalar<DataSource> {

    /**
     * Data source factory.
     */
    @SuppressWarnings("PMD.AvoidDuplicateLiterals")
    private static final IoCheckedFunc<Farm, DataSource> FACTORY =
        new IoCheckedFunc<>(
            new SolidFunc<>(
                farm -> {
                    final Props props = new Props(farm);
                    final BasicDataSource dsrc = new BasicDataSource();
                    dsrc.setDriverClassName("org.postgresql.Driver");
                    if (props.has("//testing")) {
                        dsrc.setUrl(
                            String.format(
                                "jdbc:postgresql://localhost:%s/test",
                                System.getProperty("pgsql.port")
                            )
                        );
                        dsrc.setUsername("test");
                        dsrc.setPassword("test");
                    } else {
                        dsrc.setUrl(
                            String.format(
                                "jdbc:postgresql://%s:%s/%s",
                                props.get("//pgsql//host"),
                                props.get("//pgsql/port"),
                                props.get("//pgsql/database")
                            )
                        );
                        dsrc.setUsername(props.get("//pgsql/user"));
                        dsrc.setPassword(props.get("//pgsql/password"));
                        dsrc.setMinIdle(Tv.TEN);
                        dsrc.setMaxIdle(Tv.TWENTY);
                    }
                    return dsrc;
                }
            )
        );

    /**
     * Farm.
     */
    private final Farm frm;

    /**
     * Ctor.
     *
     * @param farm Farm
     */
    public ExtDataSource(final Farm farm) {
        this.frm = farm;
    }

    @Override
    public DataSource value() throws IOException {
        return ExtDataSource.FACTORY.apply(this.frm);
    }
}
