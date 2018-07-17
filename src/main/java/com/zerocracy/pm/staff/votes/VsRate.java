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
package com.zerocracy.pm.staff.votes;

import com.jcabi.log.Logger;
import com.zerocracy.Project;
import com.zerocracy.cash.Cash;
import com.zerocracy.pm.cost.Rates;
import com.zerocracy.pm.staff.Votes;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import org.cactoos.collection.Filtered;
import org.cactoos.iterable.Mapped;
import org.cactoos.map.MapEntry;
import org.cactoos.map.SolidMap;
import org.cactoos.scalar.IoCheckedScalar;
import org.cactoos.scalar.SolidScalar;

/**
 * Lowest rate wins.
 *
 * @since 1.0
 */
public final class VsRate implements Votes {

    /**
     * Rates of others.
     */
    private final IoCheckedScalar<Map<String, Cash>> rates;

    /**
     * Ctor.
     * @param project The project
     * @param others All other logins in the competition
     * @checkstyle AvoidInlineConditionalsCheck (30 lines)
     */
    public VsRate(final Project project, final Collection<String> others) {
        this.rates = new IoCheckedScalar<>(
            new SolidScalar<>(
                () -> {
                    final Rates all = new Rates(project).bootstrap();
                    return new SolidMap<String, Cash>(
                        new Mapped<>(
                            login -> new MapEntry<>(
                                login,
                                all.exists(login) ? all.rate(login) : Cash.ZERO
                            ),
                            others
                        )
                    );
                }
            )
        );
    }

    @Override
    public double take(final String login, final StringBuilder log)
        throws IOException {
        final Cash mine = this.rates.value().get(login);
        final int cheaper = new Filtered<>(
            rate -> rate.compareTo(mine) < 0,
            this.rates.value().values()
        ).size();
        log.append(
            Logger.format(
                "Hourly rate %s is no.%d",
                mine, cheaper + 1
            )
        );
        return 1.0d - (double) cheaper / (double) this.rates.value().size();
    }
}
