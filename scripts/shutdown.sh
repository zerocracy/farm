#!/bin/bash

curl -H "X-Auth: $1" -f https://www.0crat.com/shutdown > /dev/null