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

import lombok.EqualsAndHashCode;

/**
 * Brigade claim sameness criteria.
 * @author Kirill (g4s8.public@gmail.com)
 * @version $Id$
 * @since 0.16.1
 */
@EqualsAndHashCode(of = {"type", "prj"})
@SuppressWarnings({"PMD.SingularField", "PMD.UnusedPrivateField"})
final class StkCriteria {

    /**
     * Type.
     */
    private final String type;

    /**
     * Project.
     */
    private final String prj;

    /**
     * Ctor.
     * @param type Type
     * @param project Project
     */
    StkCriteria(final String type, final String project) {
        this.type = type;
        this.prj = project;
    }
}
