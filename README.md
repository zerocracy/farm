<img src="http://www.zerocracy.com/logo.svg" width="64px" height="64px"/>

[![Availability at SixNines](http://www.sixnines.io/b/2b3a)](http://www.sixnines.io/h/2b3a)
[![Run Status](https://api.shippable.com/projects/58469fb83ee1d30f00c9b66e/badge?branch=master)](https://app.shippable.com/projects/58469fb83ee1d30f00c9b66e)
[![PDD status](http://www.0pdd.com/svg?name=zerocracy/farm)](http://www.0pdd.com/p?name=zerocracy/farm)

Stakeholders' Farm.

It's a manager of Java stakeholders.

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

## License

Copyright (c) 2016-2017 Zerocracy

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
