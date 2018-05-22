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
import java.io.Closeable;
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
 * @since 1.0
 * @todo #565:30min Implement method pay() that will write details about
 *  payment to the xml file in this format:
 *  <payments>
 *      <payment>
 *          <target>trg</target>
 *          <amount>$0.55</amount>
 *          <details>dtls</details>
 *          <result>E0885448-5DEE-11E8-9C2D-FA7AE01BBEBC</result>
 *      </payment>
 *  </payments>
 *  Unignore relevant test case from FkBankTest.
 * @todo #565:30min Implement method fee() that will write details about
 *  fee to the xml file in this format:
 *  <fees>
 *      <fee>
 *          <amount>$0.50</amount>
 *          <result>$0.80</result>
 *      </fee>
 *  </fees>
 *  Unignore relevant test case from FkBankTest.
 * @todo #566:30min Implement equals so that it conforms the relevant test
 *  case from FkBankTest. Implement relevant to equals hashcode method.
 *  Implement toString() method, that will print the content of the underlying
 *  xml file. Cover with required test cases.
 * @todo #565:30min Add FkBank to the Payroll under the file payment method.
 *  Ensure, that the opened files are closed properly and cover Payroll with
 *  tests.
 */
final class FkBank implements Bank, Closeable {

    /**
     * Location of the file.
     */
    private final Scalar<Path> file;

    /**
     * Ctor.
     */
    public FkBank() {
        this(
            () -> Files.createTempFile("fkbnk", ".xml")
        );
    }

    /**
     * Ctor.
     * @param path Path of the file
     */
    public FkBank(final Path path) {
        this(
            () -> {
                path.toFile().getParentFile().mkdirs();
                return path;
            }
        );
    }

    /**
     * Ctor.
     * @param path Path of the file
     */
    public FkBank(final Scalar<Path> path) {
        this.file = new SyncScalar<>(new StickyScalar<>(path));
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
        Files.delete(new IoCheckedScalar<>(this.file).value());
    }
}
