#!/usr/bin/env bash
# MySQL Workstation CLI Helper for courier_jdbc
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
export PATH="/usr/local/mysql/bin:$PATH"

if [ $# -eq 0 ]; then
  echo "================================================================================"
  echo "  🐬  COURIER MYSQL WORKSTATION (Interactive Console)"
  echo "  Host: 127.0.0.1 | Port: 3307 | User: courier | Database: courier_jdbc"
  echo "================================================================================"
  /usr/local/mysql/bin/mysql -u courier -pcourier_password -P 3307 -h 127.0.0.1 courier_jdbc
else
  /usr/local/mysql/bin/mysql -u courier -pcourier_password -P 3307 -h 127.0.0.1 courier_jdbc "$@"
fi
