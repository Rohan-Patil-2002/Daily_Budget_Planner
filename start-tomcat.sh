#!/bin/sh
set -eu

# Use Render's assigned port, with 10000 as a fallback.
PORT="${PORT:-10000}"
export PORT

# Disable Tomcat's shutdown port.
sed -i 's/port="8005"/port="-1"/' "$CATALINA_HOME/conf/server.xml"

# Configure Tomcat's HTTP connector to use Render's port.
sed -i "s/port=\"8080\"/port=\"$PORT\"/" "$CATALINA_HOME/conf/server.xml"

# Run Tomcat in the foreground.
exec catalina.sh run
