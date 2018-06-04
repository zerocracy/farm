set -e
mvn clean install -Pqulice -Pcodenarc --errors --batch-mode --quiet
mvn clean --quiet