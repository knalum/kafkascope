rm -rf runtime-macos
rm -rf out

jlink \
  --add-modules java.base,java.compiler,java.desktop,java.management,java.naming,java.prefs,java.security.jgss,java.security.sasl,java.sql,jdk.unsupported \
  --strip-debug \
  --no-man-pages \
  --no-header-files \
  --compress=2 \
  --output runtime-macos


jpackage \
  --name MyApp \
  --app-version 1.0 \
  --type app-image \
  --runtime-image runtime-macos \
  --input target/ \
  --main-jar kafkascope-1.0-SNAPSHOT.jar \
  --main-class no.knalum.KafkaScope \
  --icon icons/logo.icns \
  --dest out/macos
