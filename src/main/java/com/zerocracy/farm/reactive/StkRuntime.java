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
package com.zerocracy.farm.reactive;

import com.jcabi.xml.XML;
import com.zerocracy.Farm;
import com.zerocracy.Project;
import com.zerocracy.SoftException;
import com.zerocracy.Stakeholder;
import com.zerocracy.farm.MismatchException;
import groovy.lang.Binding;
import groovy.lang.Script;
import java.io.IOException;
import org.codehaus.groovy.runtime.InvokerInvocationException;

/**
 * Runtime stakeholder.
 * @since 1.0
 */
public final class StkRuntime implements Stakeholder {
    /**
     * Script type.
     */
    private final Class<? extends Script> tpe;
    /**
     * Farm.
     */
    private final Farm frm;
    /**
     * Ctor.
     * @param type Script type
     * @param farm Farm
     */
    public StkRuntime(final Class<? extends Script> type, final Farm farm) {
        this.tpe = type;
        this.frm = farm;
    }

    @Override
    @SuppressWarnings({"PMD.PreserveStackTrace",
        "PMD.AvoidThrowingRawExceptionTypes"})
    public void process(final Project project, final XML claim)
        throws IOException {
        try {
            final Binding binding = new Binding();
            binding.setVariable("farm", this.frm);
            final Script script = this.tpe.newInstance();
            script.setBinding(binding);
            script.invokeMethod(
                "exec",
                new Object[]{project, claim}
            );
        } catch (final IllegalAccessException | InstantiationException
            | InvokerInvocationException ex) {
            if (ex.getCause() instanceof MismatchException) {
                throw MismatchException.class.cast(ex.getCause());
            }
            if (ex.getCause() instanceof SoftException) {
                throw SoftException.class.cast(ex.getCause());
            }
            throw new IllegalStateException(
                String.format(
                    "%s in %s",
                    ex.getClass().getCanonicalName(),
                    this.tpe.getName()
                ),
                ex
            );
        }
    }
}
