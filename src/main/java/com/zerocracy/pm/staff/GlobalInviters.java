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
package com.zerocracy.pm.staff;

import com.zerocracy.Farm;
import com.zerocracy.pmo.Pmo;
import java.util.AbstractSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.cactoos.collection.Joined;
import org.cactoos.collection.Mapped;
import org.cactoos.scalar.SolidScalar;
import org.cactoos.scalar.UncheckedScalar;

/**
 * List of users who can invite any person.
 * See https://github.com/zerocracy/farm/issues/1410
 *
 * @since 1.0
 */
public final class GlobalInviters extends AbstractSet<String> {

    /**
     * Users scalar.
     */
    private final UncheckedScalar<Set<String>> users;

    /**
     * Ctor.
     * @param farm Farm
     */
    @SuppressWarnings("PMD.UnusedFormalParameter")
    public GlobalInviters(final Farm farm) {
        super();
        this.users = new UncheckedScalar<>(
            new SolidScalar<>(
                () -> new HashSet<String>(
                    new Joined<String>(
                        new Roles(new Pmo(farm)).bootstrap().everybody(),
                        new Joined<String>(
                            new Mapped<>(
                                pkt -> {
                                    final Roles roles = new Roles(pkt)
                                        .bootstrap();
                                    return new Joined<String>(
                                        roles.findByRole("QA"),
                                        roles.findByRole("TST")
                                    );
                                },
                                farm.find("@id='C3NDPUA8L'")
                            )
                        )
                    )
                )
            )
        );
    }

    @Override
    public Iterator<String> iterator() {
        return this.users.value().iterator();
    }

    @Override
    public int size() {
        return this.users.value().size();
    }
}
