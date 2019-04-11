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
package com.zerocracy.pm.staff.votes;

import com.zerocracy.pm.staff.Votes;
import com.zerocracy.pmo.Blanks;
import com.zerocracy.pmo.Pmo;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import org.cactoos.collection.Filtered;
import org.cactoos.map.MapEntry;
import org.cactoos.map.MapOf;
import org.cactoos.map.SolidMap;
import org.cactoos.scalar.IoCheckedScalar;
import org.cactoos.scalar.SolidScalar;

/**
 * Lowest {@link com.zerocracy.pmo.Blanks} wins.
 *
 * Votes for that person with min blanks.
 *
 * @since 1.0
 */
public final class VsBlanks implements Votes {

    /**
     * Blanks of others.
     */
    private final IoCheckedScalar<Map<String, Integer>> blanks;

    /**
     * Ctor.
     * @param pmo The PMO
     * @param others All other logins in the competition
     */
    public VsBlanks(final Pmo pmo, final Collection<String> others) {
        this.blanks = new IoCheckedScalar<>(
            new SolidScalar<>(
                () -> new SolidMap<>(
                    new MapOf<>(
                        login -> new MapEntry<>(
                            login,
                            new Blanks(pmo, login)
                                .bootstrap()
                                .total()
                        ),
                        others
                    )
                )
            )
        );
    }

    @Override
    public double take(final String login, final StringBuilder log)
        throws IOException {
        final int mine = this.blanks.value().get(login);
        final int better = new Filtered<>(
            bks -> bks < mine,
            this.blanks.value().values()
        ).size();
        log.append(
            String.format("Blanks amount %d is no.%d", mine, better + 1)
        );
        return 1.0d - (double) better / (double) this.blanks.value().size();
    }
}
