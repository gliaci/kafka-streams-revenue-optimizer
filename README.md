# kafka-streams-revenue-optimizer

## Overview

This project is a POC to show how to integrate [`Kafka Connect`](https://docs.confluent.io/current/connect/index.html) and [`Kafka Streams`](https://docs.confluent.io/current/streams/index.html) client libraries into a `Spring Boot` application to implement this business use case: the calculation of the saving amount after the order optimization process

## Prerequisites

- [`Java 19`](https://www.oracle.com/java/technologies/downloads/#java19)
- [`Docker`](https://www.docker.com/)
- [`Docker-Compose`](https://docs.docker.com/compose/install/)

## Start Environment

- Open a terminal and inside `kafka-streams-revenue-optimizer` root folder run
  ```
  docker-compose up
  ```

- Wait for all Docker containers to be up and running

## Create Kafka Topics

In order to have topics in `Kafka` with more than `1` partition:

- Open a new terminal and make sure you are in `kafka-streams-revenue-optimizer` root folder

- Run the script below
  ```
  ./create-kafka-topics.sh
  ```

  It will create the topics `mysql-masterdata-EXCHANGE_RATE` for the events created by `Kafka Connect` and `orders`, `provider-codes`, `optimized-orders` topics for the order optimization saving amount calculation.

## Create connector

Steps to create the connector:

- In a terminal, in `kafka-streams-revenue-optimizer` root folder, run the following script to create the JSON serialization connector to `kafka-connect`
  ```
  ./create-connectors-jsonconverter.sh
  ```

- You can check the state of the connector running the following script
  ```
  ./check-connectors-state.sh
  ```
  - Once the connector and its task is ready (`RUNNING` state), you should see something like
    ```
    {"name":"mysql-source-exchangerates","connector":{"state":"RUNNING","worker_id":"kafka-connect:8083"},"tasks":[{"id":0,"state":"RUNNING","worker_id":"kafka-connect:8083"}],"type":"source"}
    ```


## Running Applications with Maven

- Run the **KafkaStreamsRevenueOptimizerApplication**

## Insert Exchange Rates

Follow these steps to check database connection and to insert sample data about GBP exchange rate:

- Using a database client connected to `masterdata` (`localhost:3306`) database, execute the following query to test the connection to the database table:
  ```
  SELECT * FROM masterdata.EXCHANGE_RATE;
  ```

- Insert script for sample data
  ```
  INSERT INTO EXCHANGE_RATE (EXCHANGE_RATE_DATE, FROM_CURRENCY, TO_CURRENCY, RATE, LAST_UPDATE)
  VALUES
  ('2022-11-12 00:00:00.0', 'EUR', 'GBP', '0.85', now()),
  ('2022-11-12 00:00:00.0', 'GBP', 'EUR', '1.17', now());
  ```
  
- Opening the [`Topics UI`](http://localhost:8085/) there will be two events in the topic `mysql-masterdata-EXCHANGE_RATE`

## Create Events to check Kafka Streams topology 

Steps to test Kafka Streams join logic among the event from `provider-codes` with the data into topic GlobalKTable and the final join with data of the event from `orders` topic. There is a wait step in the script (10 seconds) in order to test the Join Windows duration (set to 30 seconds)

- If not already done, install kafka scripts (for macOS)
  ```
  brew install kafka
  ```

- If not already done, update you $PATH variable
  ```
  vi .bash_profile
  ```
  - add this line
  ```
  export PATH=$PATH:/usr/local/opt/kafka/bin/
  ```

- Open a new terminal and in `kafka-streams-revenue-optimizer` root folder and run the script below
  ```
  ./create-kafka-producer-events.sh
  ```

- Opening the [`Topics UI`](http://localhost:8085/) there will be this event into the topic `optimized-orders`:
  ```
  {
    "topic": "optimized-orders",
    "key": 12345678,
    "value": {
      "orderId": 12345678,
      "providerCode": "ABCD",
      "savingAmount": 6.4
    },
    "partition": 0,
    "offset": 0
  }
  ```

## Update an Exchange Rate and create again Events to check Kafka Streams topology

- Using a database client, execute the following query:
  ```
  UPDATE EXCHANGE_RATE SET RATE = '1.19', LAST_UPDATE = now() WHERE ID = 2;
  ```

- Run again this script
  ```
  ./create-kafka-producer-events.sh
  ```

- Opening the [`Topics UI`](http://localhost:8085/) there will be this event into the topic `optimized-orders`:
  ```
  {
    "topic": "optimized-orders",
    "key": 12345678,
    "value": {
      "orderId": 12345678,
      "providerCode": "ABCD",
      "savingAmount": 4.8
    },
    "partition": 0,
    "offset": 1
  }
  ```