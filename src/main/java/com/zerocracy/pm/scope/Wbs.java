/**
 * Copyright (c) 2016 Zerocracy
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
package com.zerocracy.pm.scope;

import com.jcabi.xml.XMLDocument;
import com.jcabi.xml.XSLDocument;
import com.zerocracy.jstk.Item;
import com.zerocracy.jstk.Project;
import com.zerocracy.pm.Job;
import com.zerocracy.pm.Xocument;
import java.io.IOException;
import org.xembly.Directives;

/**
 * WBS.
 *
 * <p>The WBS is a hierarchical decomposition of the total scope
 * of work to be carried out by the project team to accomplish
 * the project objectives and create the required deliverables.
 * The WBS organizes and defines the total scope of the project,
 * and represents the work specified in the current approved
 * project scope statement.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.1
 */
public final class Wbs {

    /**
     * Project.
     */
    private final Project project;

    /**
     * Ctor.
     * @param pkt Project
     */
    public Wbs(final Project pkt) {
        this.project = pkt;
    }

    /**
     * Bootstrap it.
     * @throws IOException If fails
     */
    public void bootstrap() throws IOException {
        try (final Item wbs = this.item()) {
            new Xocument(wbs.path()).bootstrap("wbs", "pm/scope/wbs");
        }
    }

    /**
     * Print it to text.
     * @return Text
     * @throws IOException If fails
     */
    public String print() throws IOException {
        try (final Item wbs = this.item()) {
            return new XSLDocument(
                Wbs.class.getResource("wbs/to-text.xsl")
            ).applyTo(new XMLDocument(wbs.path().toFile()));
        }
    }

    /**
     * Add job to WBS.
     * @param job The job to add
     * @throws IOException If fails
     */
    public void add(final Job job) throws IOException {
        try (final Item wbs = this.item()) {
            new Xocument(wbs.path()).modify(
                new Directives()
                    .xpath("/wbs")
                    .add("job").add("name").set(job.name())
            );
        }
    }

    /**
     * Remove job from WBS.
     * @param job The job to remove
     * @throws IOException If fails
     */
    public void remove(final Job job) throws IOException {
        try (final Item wbs = this.item()) {
            new Xocument(wbs.path()).modify(
                new Directives().xpath(
                    String.format(
                        "/wbs/job[name='%s']", job.name()
                    )
                ).remove()
            );
        }
    }

    /**
     * The item.
     * @return Item
     * @throws IOException If fails
     */
    private Item item() throws IOException {
        return this.project.acq("wbs.xml");
    }

}
