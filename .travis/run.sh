set -e
mvn clean install -Pqulice,codenarc --errors --batch-mode --quiet
mvn clean --quiet