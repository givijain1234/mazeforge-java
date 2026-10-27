#!/usr/bin/env bash
# Compiles the Java sources, runs the JUnit 5 tests and refreshes web/mazeforge.jar.
# Needs JDK 11+ (tested with JDK 17) and curl. The JUnit console jar is downloaded once into lib/.
set -euo pipefail
cd "$(dirname "$0")"
JUNIT=lib/junit-platform-console-standalone-1.10.2.jar
if [ ! -f "$JUNIT" ]; then
  mkdir -p lib
  curl -fsSL -o "$JUNIT" \
    https://repo1.maven.org/maven2/org/junit/platform/junit-platform-console-standalone/1.10.2/junit-platform-console-standalone-1.10.2.jar
fi
rm -rf build && mkdir -p build/main build/test
javac --release 11 -d build/main $(find src/main -name '*.java')
javac --release 11 -cp "build/main:$JUNIT" -d build/test $(find src/test -name '*.java')
java -jar "$JUNIT" execute -cp build/main:build/test --scan-classpath
jar cf web/mazeforge.jar -C build/main .
echo "Built web/mazeforge.jar"
