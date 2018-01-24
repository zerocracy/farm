/**
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
package com.zerocracy.tools;

import com.google.common.io.Files;
import com.jcabi.log.VerboseProcess;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import org.cactoos.Input;
import org.cactoos.io.InputOf;
import org.cactoos.io.LengthOf;
import org.cactoos.io.TeeInput;

/**
 * LaTeX document.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.20
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class Latex {

    /**
     * The source.
     */
    private final Input source;

    /**
     * Ctor.
     * @param src LaTeX source
     */
    public Latex(final String src) {
        this(new InputOf(src));
    }

    /**
     * Ctor.
     * @param src LaTeX source
     */
    public Latex(final Input src) {
        this.source = src;
    }

    /**
     * Create PDF file.
     * @return PDF file location
     * @throws IOException If fails
     */
    public Input pdf() throws IOException {
        final File dir = Files.createTempDir();
        final File doc = new File(dir, "document.tex");
        new LengthOf(new TeeInput(this.source, doc)).intValue();
        for (int idx = 0; idx < 2; ++idx) {
            Latex.compile(dir, doc.getName());
        }
        return new InputOf(new File(dir, "document.pdf"));
    }

    /**
     * Compile latex in the directory.
     * @param dir Directory
     * @param file File name
     * @throws IOException If fails
     */
    private static void compile(final File dir, final String file)
        throws IOException {
        final String out = new VerboseProcess(
            new ProcessBuilder().command(
                "pdflatex",
                "-interaction=errorstopmode",
                "-halt-on-error",
                file
            ).directory(dir).start(),
            Level.INFO,
            Level.WARNING
        ).stdout();
        if (out.contains("Error")) {
            throw new IllegalArgumentException("failed to compile latex");
        }
    }

}
