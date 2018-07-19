set -e
pdd --source=$(pwd) --verbose --file=/dev/null
mvn clean install -Pqulice,codenarc --errors --batch-mode
mvn clean --quiet
