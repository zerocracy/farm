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
import com.zerocracy.pmo.Pmo;
import com.zerocracy.pmo.Speed;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.cactoos.collection.Filtered;
import org.cactoos.iterable.Mapped;
import org.cactoos.map.MapEntry;
import org.cactoos.map.SolidMap;
import org.cactoos.scalar.IoCheckedScalar;
import org.cactoos.scalar.SolidScalar;

/**
 * Highest speed (lowest value) wins.
 *
 * Votes for that person who is the fastest.
 * Returns 1 for fast person and 0 for slow, 0.5 - for middle.
 *
 * @since 1.0
 */
public final class VsSpeed implements Votes {

    /**
     * Speeds of others.
     */
    private final IoCheckedScalar<Map<String, Double>> speeds;

    /**
     * Ctor.
     * @param pmo The PMO
     * @param others All other logins in the competition
     */
    public VsSpeed(final Pmo pmo, final Collection<String> others) {
        this.speeds = new IoCheckedScalar<>(
            new SolidScalar<>(
                () -> new SolidMap<>(
                    new Mapped<>(
                        login -> {
                            final Speed speed = new Speed(pmo, login)
                                .bootstrap();
                            final double avg;
                            if (speed.isEmpty()) {
                                avg = Double.MAX_VALUE;
                            } else {
                                avg = speed.avg();
                            }
                            return new MapEntry<>(login, avg);
                        },
                        others
                    )
                )
            )
        );
    }

    @Override
    public double take(final String login, final StringBuilder log)
        throws IOException {
        final double mine = this.speeds.value().get(login);
        final int faster = new Filtered<>(
            speed -> speed < mine,
            this.speeds.value().values()
        ).size();
        log.append(
            Logger.format(
                "Average delivery time %[ms]s is no.%d",
                (long) mine * TimeUnit.MINUTES.toMillis(1L),
                faster + 1
            )
        );
        return 1.0d - (double) faster / (double) this.speeds.value().size();
    }
}
