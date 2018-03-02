<img src="https://www.0crat.com/svg/logo.svg" width="64px" height="64px"/>

[![EO principles respected here](http://www.elegantobjects.org/badge.svg)](http://www.elegantobjects.org)
[![Managed by Zerocracy](https://www.0crat.com/badge/C3NDPUA8L.svg)](https://www.0crat.com/p/C3NDPUA8L)
[![DevOps By Rultor.com](http://www.rultor.com/b/zerocracy/farm)](http://www.rultor.com/p/zerocracy/farm)
[![We recommend IntelliJ IDEA](http://www.elegantobjects.org/intellij-idea.svg)](https://www.jetbrains.com/idea/)

[![Stability of Webhook](http://www.rehttp.net/b?u=http%3A%2F%2Fwww.0crat.com%2Fghook)](http://www.rehttp.net/i?u=http%3A%2F%2Fwww.0crat.com%2Fghook)
[![Availability at SixNines](http://www.sixnines.io/b/2b3a)](http://www.sixnines.io/h/2b3a)
[![Build Status](https://travis-ci.org/zerocracy/farm.svg?branch=master)](https://travis-ci.org/zerocracy/farm)
[![PDD status](http://www.0pdd.com/svg?name=zerocracy/farm)](http://www.0pdd.com/p?name=zerocracy/farm)

Stakeholders' Farm

It's a core repository of Zerocrat. It contains our persistence layer (`com.zerocracy.farm`),
a collection of Java stakeholders (`com.zerocracy.stk`) and interface layer for the
integration with Slack, GitHub, Telegram, and so on (`com.stakeholder.radars`).

The data model (XML, XSD, XSL documents) is in
[zerocracy/datum](https://github.com/zerocracy/datum) repository. They
are released separately and have different versions.

## Claims

The central point of control in the project is
[`claims.xml`](https://github.com/zerocracy/datum/blob/master/xsd/pm/claims.xsd) file, which
stores all requests for actions, so called "claims." Say, someone wants
to add a new job to the WBS, either a user or a software module. This
claim has to be added:

```xml
<claim id="5">
  <type>Add job to WBS</type>
  <created>2016-12-29T09:03:21.684Z</created>
  <author>yegor256</author>
  <token>slack;C43789437;yegor256</token>
  <params>
    <param name="job">gh:test/test#1</param>
  </params>
</claim>
```

Stakeholder is a software module that replies to a claim. All stakeholders
are Groovy scripts from `com.zerocracy.stk` package.

Here, `type` is a unique type of the claim, which will be used by
"stakeholders," to decide which claim to process or to ignore. The
`author` is the optional GitHub login of the person who submitted the claim;
it's empty if the claim is coming from a software module or another
stakeholder. The `token` is the location of the place where the response
is expected; in this example the response is expected in Slack channel
`C43789437` and has to be addressed to `@yegor256`. The `params` is just
an associative array of parameters.

One of the stakeholders will find that claim and reply to it. To read the
claim we use `com.zerocracy.pm.ClaimIn`, which helps proceeding the XML. To
generate a claim we use `com.zerocracy.pm.ClaimOut`.

## Farm, Project, Item

A **farm** is collection of projects.
A **project** is a collection of items.
An **item** is just a file, in most cases in XML format.

For example, in order to assign a `DEV` role to @yegor256 in `C63314D6Z`, we
should do this (provided, we already have the `farm`):

```java
Project project = farm.find("@id='C63314D6Z'").get(0);
try (Item item = project.acq("roles.xml")) {
  new Xocument(item).modify(
    new Directives()
      .xpath("/roles/people[@id='yegor256']")
      .add("role")
      .set("DEV")
  );
}
```

Here, we use `find()` in order to retrieve a list of projects by the
provided XPath term `@id='C63314D6Z'`. They will be found inside `people.xml`
and returned, if found. If a project is not found, it will be created
by `find()`.

Then, we use `acq()` to find and lock the file `roles.xml` in the project.
Until `item.close()` is called, no other thread will be able to acquire
any file in the project.

Then, we modify the file using `Xocument`, which is a helper created
exactly for XML reading and modifications of items. We provide it a list
of [Xembly](https://github.com/yegor256/xembly) directives and it
applies them to the XML document. It takes care about versioning and XSD
validation.

## Stakeholders

A **stakeholder** is a software module (object of interface `Stakeholder`)
that consumes claims. As soon
as a new claims shows up in `claims.xml`, the classes from
`com.zerocracy.farm.reactive` try to send it to all known stakeholders. We
write them in [Groovy](http://groovy-lang.org/) and keep in
`com.zerocracy.stk` package. For example, this stakeholder may react to
a claim that requests to assign a new role to a user in a project:

```groovy
def exec(Project project, XML xml) {
  new Assume(project, xml).notPmo()
  new Assume(project, xml).type('Assign role')
  new Assume(project, xml).roles('ARC', 'PO')
  ClaimIn claim = new ClaimIn(xml)
  String login = claim.param('login')
  String role = claim.param('role')
  new Roles(project).bootstrap().assign(login, role)
  claim.copy()
    .type('Role was assigned')
    .postTo(project)
  claim.reply(
    new Par('Role %s was assigned to @%s').say(role, login)
  ).postTo(project)
```

First, we use `Assume` in order to filter out incoming claims that we don't
need. Remember, each stakeholder receives all claims in a project. This
particular stakeholder needs just one claim of type `"Assign role"`. We
also allow only the architect (`ARC`) and the product owner (`PO`) to send
those role-assigning claims.

Then, we create a very convenient helper class `ClaimIn`, which is designed
to simplify our work with the incoming XML claim.

Then, we take `login` and `role` out of the claim. They are the parameters
of the claim.

Then, we do the actual work of assigning the role to the user. Pay attention
to the `.bootstrap()` call on `Roles`. It is important to always call
those `boostrap()` methods on all data-representing objects, in order to ensure
that the XML documents they represent are fully ready.

Next, we create a new claim and post back to the project. We use `.copy()`
in order to copy the incoming claim entirely. The outcoming claim of
type `"Role was assigned"` will contain the same set of parameters as the
incoming one had.

Then, we reply to the original claim with a user-friendly message. If the
incoming claim had an author (a real user), that user will receive a message,
either in Telegram, or Slack or wherever that claim was submitted.

Pay attention to the class `Par` we are using in order to format the message.
This class is supposed to be used everywhere, since it formats the text
correctly for all possible output devices and messengers.

## Radars

There are a number of entry points, where users can communicate with our
chat bots, they are all implemented in `com.zerocracy.radars.*` packages. Each
bot has its own implementation details, because systems are very different
(Telegram, Slack, GitHub, etc.). The common part is the `Question` class,
that parses the questions and translates them to claims.

## Objects

"Data-representing" objects stay in `com.zerocracy.pm` and `com.zerocracy.pmo`
packages. They mostly represent XML documents from the storage, one class
per document, e.g. `Boosts` for `boosts.xml` or `Roles` for `roles.xml`.
They all are pretty straight-forward XML manipulators, where
[jcabi-xml](https://github.com/jcabi/jcabi-xml) is used for XML reading
and [Xembly](https://github.com/yegor256/xembly) for XML modifications.

Validations are also supposed to happen in these objects. The majority
of data problems will be filtered out by XSD Schemas, but not all of them.
Sometimes we need Java to do the work of data validating. If it's needed,
we try to validate the data in data-representing objects.

## How to contribute

Just fork it, make changes, run `mvn clean install -Pqulice`, and submit
a pull request. Read
[this](http://www.yegor256.com/2014/04/15/github-guidelines.html), if lost.

## License

Copyright (c) 2016-2018 Zerocracy

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to read
the Software only. Permissions is hereby NOT GRANTED to use, copy, modify,
merge, publish, distribute, sublicense, and/or sell copies of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

