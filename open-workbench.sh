#!/usr/bin/env bash
# Launch MySQL Workstation connected to courier_jdbc
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

export DB_URL='jdbc:mysql://127.0.0.1:3307/courier_jdbc?connectionTimeZone=UTC&forceConnectionTimeZoneToSession=true'
export DB_USERNAME='courier'
export DB_PASSWORD='courier_password'

echo "================================================================================"
echo " 🐬 Launching MySQL Workstation (Logged in to courier_jdbc @ 127.0.0.1:3307)"
echo "================================================================================"

java -cp "$DIR/target/classes:$DIR/target/dependency/*" courier.MySQLWorkstation "$@"
