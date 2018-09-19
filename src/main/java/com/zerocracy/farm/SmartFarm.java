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

import com.zerocracy.Farm;
import com.zerocracy.Project;
import com.zerocracy.entry.ExtFarm;
import com.zerocracy.farm.footprint.FtFarm;
import com.zerocracy.farm.props.PropsFarm;
import com.zerocracy.farm.ruled.RdFarm;
import com.zerocracy.farm.strict.StrictFarm;
import com.zerocracy.farm.sync.SyncFarm;
import java.io.IOException;
import org.cactoos.scalar.IoCheckedScalar;
import org.cactoos.scalar.SolidScalar;

/**
 * Smart farm.
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@SuppressWarnings("PMD.ExcessiveImports")
public final class SmartFarm implements Farm {

    /**
     * Unique self.
     */
    private final IoCheckedScalar<Farm> self;

    /**
     * Ctor.
     * @param farm Original
     */
    public SmartFarm(final Farm farm) {
        this.self = new IoCheckedScalar<>(
            new SolidScalar<>(
                () -> new RdFarm(
                    new FtFarm(
                        new ExtFarm(
                            new PropsFarm(
                                new StrictFarm(
                                    new SyncFarm(farm)
                                )
                            )
                        )
                    )
                )
            )
        );
    }

    @Override
    public Iterable<Project> find(final String xpath) throws IOException {
        return this.self.value().find(xpath);
    }

    @Override
    public void close() throws IOException {
        this.self.value().close();
    }
}
