<img src="http://www.zerocracy.com/logo.svg" width="64px" height="64px"/>

[![Build Status](https://travis-ci.org/zerocracy/farm.svg?branch=master)](https://travis-ci.org/zerocracy/farm)

Stakeholders' Farm.

It is routine manager of Java stakeholders. On each run it performs
this simple algorithm:

  * Picks the next `crew` from the list
  * `crew.deploy(farm)`
  * The `crew` finds projects: `list = farm.find('?')`
  * `list.forEach(p -> p.employ(stk))`, where `stk` is runnable

Ideally, all of that should happen in parallel threads, to enable
high throughput and concurrency.
