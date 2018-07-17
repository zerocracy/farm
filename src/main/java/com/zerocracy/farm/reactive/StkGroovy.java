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
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyCodeSource;
import groovy.lang.GroovyShell;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import lombok.EqualsAndHashCode;
import org.cactoos.Input;
import org.cactoos.func.IoCheckedBiFunc;
import org.cactoos.func.StickyBiFunc;
import org.cactoos.func.SyncBiFunc;
import org.cactoos.text.TextOf;

/**
 * Stakeholder in Groovy.
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@EqualsAndHashCode(of = "label")
public final class StkGroovy implements Stakeholder {

    /**
     * Scripts.
     */
    private static final IoCheckedBiFunc<String, String, Class<?>> SCRIPTS =
        new IoCheckedBiFunc<>(
            new SyncBiFunc<>(
                new StickyBiFunc<String, String, Class<?>>(
                    (body, name) -> {
                        try (final GroovyClassLoader loader =
                            new GroovyClassLoader()) {
                            return loader.parseClass(
                                new GroovyCodeSource(
                                    body, name, GroovyShell.DEFAULT_CODE_BASE
                                )
                            );
                        }
                    }
                )
            )
        );

    /**
     * Input.
     */
    private final Input input;

    /**
     * Label.
     */
    private final String label;

    /**
     * Farm.
     */
    private final Farm farm;

    /**
     * Ctor.
     * @param src Input
     * @param lbl Label
     * @param frm Farm
     */
    public StkGroovy(final Input src, final String lbl,
        final Farm frm) {
        this.input = src;
        this.label = lbl;
        this.farm = frm;
    }

    @Override
    @SuppressWarnings({"PMD.PreserveStackTrace",
        "PMD.AvoidThrowingRawExceptionTypes"})
    public void process(final Project project, final XML claim)
        throws IOException {
        final Binding binding = new Binding();
        binding.setVariable("farm", this.farm);
        final Class<?> clazz = StkGroovy.SCRIPTS.apply(
            new TextOf(this.input).asString(), this.label
        );
        try {
            final Constructor<?> constructor = clazz.getConstructor(
                Binding.class
            );
            final Object instance = constructor.newInstance(binding);
            clazz.getMethod("exec", Project.class, XML.class)
                .invoke(instance, project, claim);
        } catch (final IllegalAccessException | NoSuchMethodException
            | InstantiationException | InvocationTargetException ex) {
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
                    this.label
                ),
                ex
            );
        }
    }
}
