#!/bin/bash
#
# Start script for orders.api.gov.uk

PORT=8080

exec java -jar -Dserver.port="${PORT}" "orders.api.ch.gov.uk.jar"