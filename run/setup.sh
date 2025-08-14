#!/bin/bash

# 1. Install Java 17 and Maven
apt-get update -qq
apt-get install -yqq openjdk-17-jdk maven

# 2. Verify Java version
java -version
javac -version
mvn -v

# 3. Configure Maven to use Codex proxy
mkdir -p ~/.m2
cat > ~/.m2/settings.xml <<EOF
<settings>
  <proxies>
    <proxy>
      <id>codexProxy</id>
      <active>true</active>
      <protocol>http</protocol>
      <host>proxy</host>
      <port>8080</port>
    </proxy>
  </proxies>
</settings>
EOF

# 4. Download all dependencies (while internet is active)
mvn dependency:go-offline -B