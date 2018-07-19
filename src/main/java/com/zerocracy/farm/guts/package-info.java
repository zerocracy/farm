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
 * Guts.
 *
 * <p>This is a rather primitive supplementary monitoring system,
 * to allow us check what's going on inside all those Farm decorators.
 * The full report is rendered by {@link com.zerocracy.farm.guts.TkGuts}.</p>
 *
 * <p>Each Farm, if it wants to expose something, returns an instance
 * of class {@link com.zerocracy.farm.guts.Guts} out of its method
 * {@link com.zerocracy.Farm#find(java.lang.String)}. The list of projects
 * will contain only one project {@link com.zerocracy.farm.guts.GsProject},
 * if the query is equal to {@link com.zerocracy.farm.guts.Guts#QUERY}.
 * This is exactly what {@link com.zerocracy.farm.guts.TkGuts} is using
 * as a query string.</p>
 *
 * <p>The returned {@link com.zerocracy.farm.guts.GsProject} builds
 * a Xembly response with some system information, injected there by
 * every corresponding Farm.</p>
 *
 * @since 1.0
 */
package com.zerocracy.farm.guts;
