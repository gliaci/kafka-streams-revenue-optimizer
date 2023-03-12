#!/usr/bin/env bash

echo
echo "Creating event to orders topic"
echo "------------------------------------"
kafka-console-producer --broker-list localhost:29092 --topic orders --property "parse.key=true" --property "key.separator=," < events/orderEvent.txt

echo "Waiting 10 seconds..."
sleep 10

echo
echo "Creating event to retrieved-pnrs topic"
echo "------------------------------------"
kafka-console-producer --broker-list localhost:29092 --topic retrieved-pnrs --property "parse.key=true" --property "key.separator=," < events/retrievedPnrEvent.txt