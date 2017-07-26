/**
 * Copyright (c) 2016-2017 Zerocracy
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
package com.zerocracy.farm.sync;

import java.util.AbstractMap;
import java.util.Map;

/**
 * Comparable map entry.
 *
 * @author Kirill (g4s8.public@gmail.com)
 * @version $Id$
 * @param <K> Key type
 * @param <V> Value type
 * @since 0.12
 */
final class CmpEntry<K, V extends Comparable<? super V>> extends
    AbstractMap.SimpleImmutableEntry<K, V> implements
    Comparable<CmpEntry<K, V>> {

    private static final long serialVersionUID = 6039678934863820533L;

    /**
     * Ctor.
     *
     * @param origin Origin map entry
     */
    CmpEntry(final Map.Entry<K, V> origin) {
        super(origin);
    }

    @Override
    public int compareTo(final CmpEntry<K, V> other) {
        return getValue().compareTo(other.getValue());
    }
}
