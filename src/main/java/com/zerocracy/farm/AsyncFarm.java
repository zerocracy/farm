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
package com.zerocracy.farm;

import com.zerocracy.jstk.Farm;
import com.zerocracy.jstk.Project;
import com.zerocracy.jstk.Stakeholder;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Asynchronous farm.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.1
 */
final class AsyncFarm implements Farm {

    /**
     * Original farm.
     */
    private final Farm origin;

    /**
     * Service.
     */
    private final ExecutorService service;

    /**
     * Ctor.
     * @param farm Original farm
     */
    AsyncFarm(final Farm farm) {
        this.origin = farm;
        this.service = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors() << 2
        );
    }

    @Override
    public Iterable<Project> find(final String query) throws IOException {
        return this.origin.find(query);
    }

    @Override
    public void deploy(final Stakeholder stakeholder) {
        this.service.submit(
            () -> {
                this.origin.deploy(stakeholder);
                return null;
            }
        );
    }
}
