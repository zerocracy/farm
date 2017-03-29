<img src="http://www.zerocracy.com/logo.svg" width="64px" height="64px"/>

[![Availability at SixNines](http://www.sixnines.io/b/2b3a)](http://www.sixnines.io/h/2b3a)
[![Run Status](https://api.shippable.com/projects/58469fb83ee1d30f00c9b66e/badge?branch=master)](https://app.shippable.com/projects/58469fb83ee1d30f00c9b66e)
[![PDD status](http://www.0pdd.com/svg?name=zerocracy/farm)](http://www.0pdd.com/p?name=zerocracy/farm)

Stakeholders' Farm.

It is routine manager of Java stakeholders. On each run it performs
this simple algorithm:

  * Picks the next `crew` from the list
  * `crew.deploy(farm)`
  * The `crew` finds projects: `list = farm.find('?')`
  * `list.forEach(p -> farm.employ(stk))`, where `stk` is runnable

Ideally, all of that should happen in parallel threads, to enable
high throughput and concurrency.

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
