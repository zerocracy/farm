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
package com.zerocracy.radars.slack;

import com.ullink.slack.simpleslackapi.SlackPersona;

/**
 * Fake {@link SlackPersona}.
 * @since 0.28
 */
@SuppressWarnings({"PMD.TooManyMethods", "PMD.AvoidDuplicateLiterals"})
public final class FkPersona implements SlackPersona {

    @Override
    public String getId() {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public String getUserName() {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public String getRealName() {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public String getUserMail() {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public String getUserSkype() {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public String getUserPhone() {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public String getUserTitle() {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public boolean isDeleted() {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public boolean isAdmin() {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public boolean isOwner() {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public boolean isPrimaryOwner() {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public boolean isRestricted() {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public boolean isUltraRestricted() {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public boolean isBot() {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public String getTimeZone() {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public String getTimeZoneLabel() {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public Integer getTimeZoneOffset() {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public SlackPresence getPresence() {
        throw new IllegalStateException("Not implemented");
    }
}
