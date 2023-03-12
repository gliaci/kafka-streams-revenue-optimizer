#!/usr/bin/env bash

echo
echo "Creating compact topic mysql-masterdata-EXCHANGE_RATE"
echo "------------------------------------"
docker exec -t zookeeper kafka-topics --create --bootstrap-server kafka:9092 --replication-factor 1 --partitions 2 --topic mysql-masterdata-EXCHANGE_RATE --config retention.ms=-1

echo
echo "Creating topic orders"
echo "------------------------------------"
docker exec -t zookeeper kafka-topics --create --bootstrap-server kafka:9092 --replication-factor 1 --partitions 2 --topic orders

echo
echo "Creating topic provider-codes"
echo "------------------------------------"
docker exec -t zookeeper kafka-topics --create --bootstrap-server kafka:9092 --replication-factor 1 --partitions 2 --topic provider-codes

echo
echo "Creating topic optimized-orders"
echo "------------------------------------"
docker exec -t zookeeper kafka-topics --create --bootstrap-server kafka:9092 --replication-factor 1 --partitions 2 --topic optimized-orders

echo
echo "List topics"
echo "-----------"
docker exec -t zookeeper kafka-topics --list --bootstrap-server kafka:9092