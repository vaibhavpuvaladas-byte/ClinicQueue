#!/bin/sh
# ClinicQueue Maven Wrapper for Unix/Linux/macOS

PRG="$0"
while [ -h "$PRG" ]; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`/"$link"
  fi
done
SAVED="`pwd`"
cd "`dirname \"$PRG\"`" >/dev/null
APPDIR="`pwd -P`"
cd "$SAVED" >/dev/null

if command -v mvn >/dev/null 2>&1; then
  exec mvn "$@"
elif [ -f "$APPDIR/.tools/apache-maven-3.9.9/bin/mvn" ]; then
  exec "$APPDIR/.tools/apache-maven-3.9.9/bin/mvn" "$@"
else
  echo "[ERROR] Maven is not installed on PATH." >&2
  exit 1
fi
