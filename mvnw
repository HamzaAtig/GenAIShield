#!/bin/sh
# ----------------------------------------------------------------------------
# Apache Maven Wrapper startup script, version 3.3.2
#
# Required ENV vars:
#   JAVA_HOME - location of a JDK home dir
#
# Optional ENV vars:
#   MAVEN_OPTS - JVM options passed to Maven
# ----------------------------------------------------------------------------

set -euf

BASE_DIR="$(cd "$(dirname "$0")" && pwd)"
WRAPPER_DIR="$BASE_DIR/.mvn/wrapper"
WRAPPER_JAR="$WRAPPER_DIR/maven-wrapper.jar"
WRAPPER_PROPERTIES="$WRAPPER_DIR/maven-wrapper.properties"

if [ -z "${JAVA_HOME:-}" ]; then
  JAVA_CMD="java"
else
  JAVA_CMD="$JAVA_HOME/bin/java"
fi

if [ ! -f "$WRAPPER_JAR" ]; then
  if [ ! -f "$WRAPPER_PROPERTIES" ]; then
    echo "Missing $WRAPPER_PROPERTIES"
    exit 1
  fi

  WRAPPER_URL=$(grep -E '^wrapperUrl=' "$WRAPPER_PROPERTIES" | cut -d= -f2-)
  if [ -z "$WRAPPER_URL" ]; then
    echo "wrapperUrl not set in $WRAPPER_PROPERTIES"
    exit 1
  fi

  mkdir -p "$WRAPPER_DIR"

  if command -v curl >/dev/null 2>&1; then
    curl -fsSL "$WRAPPER_URL" -o "$WRAPPER_JAR"
  elif command -v wget >/dev/null 2>&1; then
    wget -q "$WRAPPER_URL" -O "$WRAPPER_JAR"
  else
    echo "Neither curl nor wget available to download Maven Wrapper."
    exit 1
  fi
fi

HAS_RUN=0
HAS_TARGET=0
for arg in "$@"; do
  case "$arg" in
    spring-boot:run|*:spring-boot:run) HAS_RUN=1 ;;
    -f|--file|-pl|--projects) HAS_TARGET=1 ;;
  esac
done

if [ "$HAS_RUN" -eq 1 ] && [ "$HAS_TARGET" -eq 0 ]; then
  set -- -f "$BASE_DIR/modules/app-api/pom.xml" "$@"
fi

exec "$JAVA_CMD" ${MAVEN_OPTS:-} -classpath "$WRAPPER_JAR" -Dmaven.multiModuleProjectDirectory="$BASE_DIR" org.apache.maven.wrapper.MavenWrapperMain "$@"
