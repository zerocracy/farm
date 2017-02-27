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
import com.zerocracy.jstk.Project;
import com.zerocracy.jstk.Stakeholder;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Stakeholder in Groovy.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.10
 */
final class GroovyStakeholder implements Stakeholder {

    /**
     * Path.
     */
    private final Path path;

    /**
     * Ctor.
     * @param file File with Groovy script
     */
    GroovyStakeholder(final Path file) {
        this.path = file;
    }

    @Override
    public void process(final Project project, final XML claim)
        throws IOException {
        final Binding binding = new Binding();
        binding.setVariable("project", project);
        binding.setVariable("claim", claim);
        final GroovyShell shell = new GroovyShell(binding);
        shell.evaluate(this.path.toFile());
    }

}
