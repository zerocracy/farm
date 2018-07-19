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
package com.zerocracy;

import com.google.common.base.Predicate;
import com.google.common.collect.Multimap;
import java.util.regex.Pattern;
import org.reflections.Configuration;
import org.reflections.scanners.Scanner;
import org.reflections.vfs.Vfs;

/**
 * Resource scanner which filter file names by pattern.
 * @since 1.0
 */
public final class PatternScanner implements Scanner {

    /**
     * Origin scanner.
     */
    private final Scanner origin;

    /**
     * Pattern for files.
     */
    private final Pattern pattern;

    /**
     * Ctor.
     * @param org Origin scanner
     * @param ptn Pattern for files.
     */
    public PatternScanner(final Scanner org, final Pattern ptn) {
        this.origin = org;
        this.pattern = ptn;
    }

    @Override
    public boolean acceptsInput(final String file) {
        return this.pattern.matcher(file).matches()
            && this.origin.acceptsInput(file);
    }

    @Override
    public void setConfiguration(final Configuration configuration) {
        this.origin.setConfiguration(configuration);
    }

    @Override
    public Multimap<String, String> getStore() {
        return this.origin.getStore();
    }

    @Override
    public void setStore(final Multimap<String, String> store) {
        this.origin.setStore(store);
    }

    @Override
    public Scanner filterResultsBy(final Predicate<String> filter) {
        return this.origin.filterResultsBy(filter);
    }

    @Override
    public Object scan(final Vfs.File file, final Object obj) {
        return this.origin.scan(file, obj);
    }

    @Override
    public boolean acceptResult(final String fqn) {
        return this.origin.acceptResult(fqn);
    }
}
