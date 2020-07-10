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
package com.zerocracy.shutdown;

import com.jcabi.log.Logger;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Shutdown hook.
 * @since 1.0
 */
public final class ShutdownHook {

    /**
     * Initial state.
     */
    private static final String ST_NONE = "none";

    /**
     * Shutdown in progress.
     */
    private static final String ST_STOPPING = "stopping";

    /**
     * Shutdown completed.
     */
    private static final String ST_STOPPED = "stopped";

    /**
     * Current state.
     */
    private final AtomicReference<String> state;

    /**
     * Default ctor.
     */
    public ShutdownHook() {
        this(new AtomicReference<>(ShutdownHook.ST_NONE));
    }

    /**
     * Primary ctor.
     *
     * @param state State
     */
    ShutdownHook(final AtomicReference<String> state) {
        this.state = state;
    }

    /**
     * Register shutdown hook.
     * @param runtime Java runtime
     */
    public void register(final Runtime runtime) {
        runtime.addShutdownHook(new Thread(this::shutdown));
    }

    /**
     * Check shutdown in progress.
     *
     * @return TRUE if in progress
     */
    public boolean stopping() {
        return this.state.get().equals(ShutdownHook.ST_STOPPING);
    }

    /**
     * Check shutdown was completed.
     *
     * @return TRUE if stopped
     */
    public boolean stopped() {
        return this.state.get().equals(ShutdownHook.ST_STOPPED);
    }

    /**
     * Check that shutdown wasn't requested.
     *
     * @return TRUE if not requested
     */
    public boolean check() {
        return this.state.get().equals(ShutdownHook.ST_NONE);
    }

    /**
     * Complete shutdown.
     */
    public void complete() {
        this.state.set(ShutdownHook.ST_STOPPED);
    }

    @Override
    public String toString() {
        return this.state.get();
    }

    /**
     * Request shutdown.
     */
    private void shutdown() {
        if (!this.state.compareAndSet(
            ShutdownHook.ST_NONE, ShutdownHook.ST_STOPPING
        )) {
            throw new IllegalStateException(
                String.format("Cant stop when %s", this.state.get())
            );
        }
        Logger.warn(this, "Shutdown");
        while (!ShutdownHook.ST_STOPPED.equals(this.state.get())) {
            try {
                TimeUnit.SECONDS.sleep(1L);
            } catch (final InterruptedException ignored) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
