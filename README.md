<img src="https://www.0crat.com/svg/logo.svg" width="64px" height="64px"/>

[![EO principles respected here](https://cdn.rawgit.com/yegor256/elegantobjects.github.io/master/badge.svg)](http://www.elegantobjects.org)
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

