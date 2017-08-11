#!/bin/bash
set -e
set -x

head=$(git rev-parse --short HEAD)
cd $(dirname $0)
trap 'git reset HEAD~1 && rm settings.xml' EXIT
cp /code/home/assets/zerocracy/settings.xml .
git add settings.xml
git commit -m 'settings.xml for heroku'
sed -i "s/\${buildNumber}/${head}/g" src/main/resources/main.properties
git add src/main/resources/main.properties
git commit -m 'build.revision updated'
git push heroku master -f

