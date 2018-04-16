set -e
mvn clean install -Pqulice --errors --batch-mode --quiet
mvn clean --quiet