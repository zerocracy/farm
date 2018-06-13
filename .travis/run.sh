set -e
pdd --source=$(pwd) --verbose --file=/dev/null
mvn clean install -Pqulice,codenarc --errors --batch-mode --quiet
mvn clean --quiet