#!/usr/bin/env bash

echo "-----------------------"
echo "Creating connector...  "
echo "-----------------------"

echo
curl -i -X POST http://localhost:8083/connectors \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json' \
  -d @connectors/jsonconverter/mysql-source-exchangerates.json

echo
echo "--------------------------------------------------------------"
echo "Connector created                                             "
echo "--------------------------------------------------------------"