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
package com.zerocracy.farm.reactive;

import com.jcabi.xml.XML;
import com.zerocracy.Xocument;
import com.zerocracy.jstk.Project;
import com.zerocracy.jstk.Stakeholder;
import groovy.lang.Binding;
import groovy.lang.GroovyCodeSource;
import groovy.lang.GroovyShell;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;

/**
 * Stakeholder in Groovy.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.10
 */
public final class StkGroovy implements Stakeholder {

    /**
     * Source.
     */
    private final GroovyCodeSource source;

    /**
     * Pairs.
     */
    private final Collection<StkGroovy.Pair> pairs;

    /**
     * Ctor.
     * @param path Path in resources
     * @param list Params
     */
    public StkGroovy(final String path, final StkGroovy.Pair... list) {
        this(
            new GroovyCodeSource(
                Xocument.class.getResource(
                    String.format("stk/%s", path)
                )
            ),
            list
        );
    }

    /**
     * Ctor.
     * @param file File with Groovy script
     * @param list Params
     * @throws IOException If fails
     */
    public StkGroovy(final Path file, final StkGroovy.Pair... list)
        throws IOException {
        this(new GroovyCodeSource(file.toFile()), list);
    }

    /**
     * Ctor.
     * @param src Source
     * @param list Params
     */
    StkGroovy(final GroovyCodeSource src, final StkGroovy.Pair... list) {
        this.source = src;
        this.pairs = Arrays.asList(list);
    }

    @Override
    public void process(final Project project, final XML claim) {
        final Binding binding = new Binding();
        binding.setVariable("project", project);
        binding.setVariable("claim", claim);
        for (final StkGroovy.Pair pair : this.pairs) {
            pair.applyTo(binding);
        }
        final GroovyShell shell = new GroovyShell(binding);
        shell.evaluate(this.source);
    }

    /**
     * Pair.
     */
    public static final class Pair {
        /**
         * Name.
         */
        private final String name;
        /**
         * Object.
         */
        private final Object object;
        /**
         * Ctor.
         * @param left Name
         * @param right Object
         */
        public Pair(final String left, final Object right) {
            this.name = left;
            this.object = right;
        }
        /**
         * Apply to the binding.
         * @param binding Binding
         */
        public void applyTo(final Binding binding) {
            binding.setProperty(this.name, this.object);
        }
    }

}
