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
import com.zerocracy.pm.staff.Votes;
import com.zerocracy.pmo.Awards;
import com.zerocracy.pmo.Pmo;
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
 * Reputation voter.
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class VsReputation implements Votes {

    /**
     * Reputations of others.
     */
    private final IoCheckedScalar<Map<String, Integer>> ranks;

    /**
     * Ctor.
     * @param pmo The PMO
     * @param others All other logins to compete with
     */
    public VsReputation(final Pmo pmo, final Collection<String> others) {
        this.ranks = new IoCheckedScalar<>(
            new SolidScalar<>(
                () -> new SolidMap<>(
                    new Mapped<>(
                        login -> new MapEntry<>(
                            login,
                            new Awards(pmo, login).bootstrap().total()
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
        final int mine = this.ranks.value().get(login);
        final int larger = new Filtered<>(
            rank -> rank > mine,
            this.ranks.value().values()
        ).size();
        log.append(
            Logger.format(
                "Reputation %+d jobs is no.%d",
                mine, larger + 1
            )
        );
        return 1.0d - (double) larger / (double) this.ranks.value().size();
    }
}
