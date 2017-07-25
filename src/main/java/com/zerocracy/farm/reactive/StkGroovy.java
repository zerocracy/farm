/**
 * Copyright (c) 2016-2017 Zerocracy
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

import com.jcabi.aspects.Cacheable;
import com.jcabi.xml.XML;
import com.zerocracy.farm.MismatchException;
import com.zerocracy.jstk.Project;
import com.zerocracy.jstk.Stakeholder;
import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import lombok.EqualsAndHashCode;
import org.cactoos.Input;
import org.cactoos.func.And;
import org.cactoos.func.UncheckedScalar;
import org.cactoos.io.InputAsBytes;
import org.cactoos.text.BytesAsText;

/**
 * Stakeholder in Groovy.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.10
 */
@EqualsAndHashCode(of = "label")
public final class StkGroovy implements Stakeholder {

    /**
     * Input.
     */
    private final Input input;

    /**
     * Label.
     */
    private final String label;

    /**
     * Deps.
     */
    private final Map<String, Object> deps;

    /**
     * Ctor.
     * @param src Input
     * @param lbl Label
     */
    public StkGroovy(final Input src, final String lbl) {
        this(src, lbl, new HashMap<>(0));
    }

    /**
     * Ctor.
     * @param src Input
     * @param lbl Label
     * @param dps Dependencies
     */
    public StkGroovy(final Input src, final String lbl,
        final Map<String, Object> dps) {
        this.input = src;
        this.label = lbl;
        this.deps = dps;
    }

    @Override
    public void process(final Project project, final XML claim)
        throws IOException {
        final Binding binding = new Binding();
        new UncheckedScalar<>(
            new And(
                this.deps.entrySet(),
                ent -> {
                    binding.setVariable(ent.getKey(), ent.getValue());
                }
            )
        ).value();
        final Class<?> clazz = this.script();
        try {
            final Constructor<?> constructor = clazz.getConstructor(
                Binding.class
            );
            final Object instance = constructor.newInstance(binding);
            clazz.getMethod("exec", Project.class, XML.class)
                .invoke(instance, project, claim);
        } catch (final IllegalAccessException | NoSuchMethodException
            | InstantiationException | InvocationTargetException exp) {
            if (exp.getCause() instanceof MismatchException) {
                throw MismatchException.class.cast(exp.getCause());
            }
            if (exp.getCause() instanceof IllegalStateException) {
                throw IllegalStateException.class.cast(exp.getCause());
            }
            throw new RuntimeException(exp);
        }
    }
    /**
     * Compiles the script.
     * @return Class
     * @throws IOException If fails
     */
    @Cacheable(forever = true)
    private Class<?> script() throws IOException {
        try (final GroovyClassLoader loader = new GroovyClassLoader()) {
            return loader.parseClass(
                new BytesAsText(
                    new InputAsBytes(this.input)
                ).asString()
            );
        }
    }
}
