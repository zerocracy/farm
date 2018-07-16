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

/**
 * Props farm.
 *
 * <p>Classes in this package are used to make sure that the
 * {@link Farm} reflects the _props.xml file containing properties such as
 * sensitive credentials used in PMO project.</p>
 *
 * <p>It is used in the decoration chain, in the constructor
 * {@link com.zerocracy.farm.SmartFarm#SmartFarm SmartFarm(Farm)}, which is the
 * Farm entry point used in {@link com.zerocracy.entry.Main#exec Main.exec()}.
 * </p>
 *
 * <p>Also, {@link com.zerocracy.farm.props.PropsFarm} may be used directly
 * in tests where PMO is needed, in order to have access to the properties.</p>
 *
 * @since 1.0
 */
package com.zerocracy.farm.props;
