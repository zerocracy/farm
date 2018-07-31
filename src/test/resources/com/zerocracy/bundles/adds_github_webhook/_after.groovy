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
package com.zerocracy.bundles.adds_github_webhook

import com.jcabi.github.Coordinates
import com.jcabi.github.Github
import com.jcabi.github.Repo
import com.jcabi.xml.XML
import com.zerocracy.Farm
import com.zerocracy.Project
import com.zerocracy.entry.ExtGithub
import org.hamcrest.MatcherAssert
import org.hamcrest.core.IsEqual

// @todo #1226:30min add_github_webhook.groovy bundletest can't be
//  properly implemented because we are not able to retrieve a valid github
//  hook instance: repo.hooks().get(0) returns a MkHook instance and MkHook
//  does not have the #json() method implemented. Wait for the resolution of
//  this case (https://github.com/jcabi/jcabi-github/issues/1425) or find
//  another solution and then uncomment the remaining tests and add another
//  test that will check that in case we don't have enough permissions we
//  will inform the user about it.
def exec(Project project, XML xml) {
  Farm farm = binding.variables.farm
  Github github = new ExtGithub(farm).value()
  Repo repo = github.repos().get(new Coordinates.Simple('test/test'))
  MatcherAssert.assertThat(
    'Hook was not created',
    repo.hooks().iterate().iterator().size(),
    new IsEqual<Integer>(1)
  )
//  Hook.Smart hook = new Hook.Smart(repo.hooks().get(0))
//  MatcherAssert.assertThat(
//    'Hook name is wrong',
//    hook.name(),
//    new IsEqual<>('web')
//  )
//  JsonObject json = hook.json()
//  MatcherAssert.assertThat(
//    'Hook is inactive',
//    json.getBoolean('active'),
//    new IsEqual<>(true)
//  )
//  MatcherAssert.assertThat(json.getBoolean('active'), Matchers.is(true))
//  MatcherAssert.assertThat(
//    json.getJsonArray('events').toListString(),
//    new IsIterableContainingInAnyOrder(new IsEqual<>('*'))
//  )
//  MatcherAssert.assertThat(
//    json.getJsonObject('config').getString('content_type'),
//    new IsEqual<>('form')
//  )
//  MatcherAssert.assertThat(
//    json.getJsonObject('config').getString('url'),
//    new StringEndsWith('/ghook')
//  )
}
