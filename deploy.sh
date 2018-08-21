#!/bin/bash
set -e
set -x

head=$(git rev-parse --short HEAD)
cd $(dirname $0)
trap 'git reset HEAD~1 && rm settings.xml && git reset --hard' EXIT
cp /code/home/assets/zerocracy/settings.xml .
git add settings.xml
sed -i "s/\${buildNumber}/${head}/g" src/main/resources/com/zerocracy/_props.xml
git add src/main/resources/com/zerocracy/_props.xml
git commit -m 'changes for heroku'
git push heroku master -f
