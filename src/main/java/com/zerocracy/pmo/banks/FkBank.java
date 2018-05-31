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
package com.zerocracy.pmo.banks;

import com.zerocracy.cash.Cash;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.cactoos.Scalar;
import org.cactoos.scalar.IoCheckedScalar;
import org.cactoos.scalar.StickyScalar;
import org.cactoos.scalar.SyncScalar;

/**
 * Fake {@link Bank}.
 *
 * <p>There is no thread-safety guarantee.</p>
 *
 * @author Tolegen Izbassar (t.izbassar@gmail.com)
 * @version $Id$
 * @since 0.22
 * @todo #565:30min Implement method pay() that will write details about
 *  payment to the xml file in this format:
 *  `&lt;payments&gt;
 *  `    &lt;payment&gt;
 *  `        &lt;target&gt;trg&lt;/target&gt;
 *  `        &lt;amount&gt;$0.55&lt;/amount&gt;
 *  `        &lt;details&gt;dtls&lt;/details&gt;
 *  `        &lt;result&gt;E0885448-5DEE-11E8-9C2D-FA7AE01BBEBC&lt;/result&gt;
 *  `    &lt;/payment&gt;
 *  `&lt;/payments&gt;
 *  Unignore relevant test case from FkBankTest.
 * @todo #565:30min Implement method fee() that will write details about
 *  fee to the xml file in this format:
 *  `&lt;fees&gt;
 *  `    &lt;fee&gt;
 *  `        &lt;amount&gt;$0.50&lt;/amount&gt;
 *  `        &lt;result&gt;$0.80&lt;/result&gt;
 *  `    &lt;/fee&gt;
 *  `&lt;/fees&gt;
 *  Unignore relevant test case from FkBankTest.
 * @todo #566:30min Implement equals so that it conforms the relevant test
 *  case from FkBankTest. Implement relevant to equals hashcode method.
 *  Implement toString() method, that will print the content of the underlying
 *  xml file. Cover with required test cases.
 * @todo #565:30min Add FkBank to the Payroll under the file payment method.
 *  Ensure, that the opened files are closed properly and cover Payroll with
 *  tests.
 */
final class FkBank implements Bank {

    /**
     * Location of the file.
     */
    private final Scalar<Path> file;

    /**
     * Delete on close? If false, deletes on JVM exit.
     */
    private final boolean delete;

    /**
     * Ctor.
     */
    FkBank() {
        this(
            () -> Files.createTempFile("fkbnk", ".xml"),
            true
        );
    }

    /**
     * Ctor.
     * @param path Path of the file
     */
    FkBank(final Path path) {
        this(
            () -> {
                path.toFile().getParentFile().mkdirs();
                return path;
            },
            true
        );
    }

    /**
     * Ctor.
     * @param path Path of the file
     * @param del Delete on close()?
     */
    FkBank(final Scalar<Path> path, final boolean del) {
        this.file = new SyncScalar<>(new StickyScalar<>(path));
        this.delete = del;
    }

    @Override
    public Cash fee(final Cash amount) throws IOException {
        throw new UnsupportedOperationException("fee is not yet implemented");
    }

    @Override
    public String pay(final String target, final Cash amount,
        final String details) throws IOException {
        throw new UnsupportedOperationException("pay is not yet implemented");
    }

    @Override
    public void close() throws IOException {
        if (this.delete) {
            Files.delete(new IoCheckedScalar<>(this.file).value());
        } else {
            new IoCheckedScalar<>(this.file).value().toFile().deleteOnExit();
        }
    }
}
