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
package com.zerocracy.bundles.cleanup_comments

import com.jcabi.xml.XML
import com.zerocracy.Farm
import com.zerocracy.Item
import com.zerocracy.Project
import com.zerocracy.entry.ExtDynamo
import com.zerocracy.entry.ExtGithub
import com.zerocracy.farm.Errors
import org.hamcrest.MatcherAssert
import org.hamcrest.collection.IsEmptyCollection
import org.hamcrest.core.IsNot

def exec(Project pmo, XML xml) {
//  Farm farm = binding.variables.farm
//  Errors.Github errors = new Errors.Github(
//    new Errors(new ExtDynamo(farm).value()),
//    new ExtGithub(farm).value()
//  )
//  MatcherAssert.assertThat(
//    "Comments not cleaned from dynamo",
//    errors.iterate(10,72L),
//    new IsEmptyCollection<>()
//  )
}
