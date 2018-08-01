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
package com.zerocracy.entry;

import com.amazonaws.auth.AWSCredentials;
import com.zerocracy.Farm;
import com.zerocracy.farm.props.Props;
import java.io.IOException;

/**
 * Credentials from {@code _props.xml}.
 *
 * @since 1.0
 */
public final class PropsAwsCredentials implements AWSCredentials {

    /**
     * Props.
     */
    private final Props props;

    /**
     * Xpath.
     */
    private final String name;

    /**
     * Ctor.
     *
     * @param farm Farm
     * @param name Credentials name
     */
    public PropsAwsCredentials(final Farm farm, final String name) {
        this(new Props(farm), name);
    }

    /**
     * Ctor.
     *
     * @param props Properties
     * @param name Credentials name
     */
    public PropsAwsCredentials(final Props props, final String name) {
        this.props = props;
        this.name = name;
    }

    @Override
    public String getAWSAccessKeyId() {
        try {
            return this.props.get(String.format("//%s/key", this.name));
        } catch (final IOException err) {
            throw new IllegalStateException("Failed to read key", err);
        }
    }

    @Override
    public String getAWSSecretKey() {
        try {
            return this.props.get(String.format("//%s/secret", this.name));
        } catch (final IOException err) {
            throw new IllegalStateException("Failed to read secret", err);
        }
    }
}
