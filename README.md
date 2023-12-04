# FashionDigitalAssignment

This repository contains multiple services within a single codebase,
provide to a simple stream/flow pipeline (producer, consumer, aggregator, and query service).

## Start App

### Step 1

To begin, start PostgreSQL locally:

```bash
docker compose -f docker-compose.yml up
docker compose -f docker-compose.yml up -d
```

This command initiates a PostgreSQL server and sets up the relational database by creating tables.

### Step 2

The next step is to start the applications:

Start producer

```bash
sbt "runMain org.ilimturan.ProducerSpeech"
```

Start consumer service (downloader, parser/transformer)
```bash
sbt "runMain org.ilimturan.ConsumerSpeech"
```

Start aggregator service
```bash
sbt "runMain org.ilimturan.AggregatorSpeech"
```

Start query service
```bash
sbt "runMain org.ilimturan.QuerySpeech"
```

### Endpoints:

[http://localhost:8080/evaluation?url=https://fid-recruiting.s3-eu-west-1.amazonaws.com/politics.csv](http://localhost:8080/evaluation?url=https://fid-recruiting.s3-eu-west-1.amazonaws.com/politics.csv)

[http://localhost:8083/report?mostYear=:mostYear&mostTopic=:mostTopic](http://localhost:8083/report?mostYear=:mostYear&mostTopic=:mostTopic)
### Stop Docker Compose

To stop the docker compose, use the following command:

```bash
docker compose -f docker-compose.yml down
```

## Tests

### Step 1

For integration tests (no need for unit tests), first, run:

```bash
docker compose -f docker-compose-test.yml up -d
```

This command creates a separate PostgreSQL test database.

### Step 2

Afterward, execute all tests (unit and integration tests):

```bash
sbt test
```

### Coverage Report

If a coverage report is needed:

```bash
sbt clean coverage test coverageReport
```

### Stop Tests

To stop the testing environment, use:

```bash
docker compose -f docker-compose-test.yml down
```

## Code Formatting

For code formatting:

```bash
sbt scalafmt
```

```bash
sbt test:scalafmt
```

## Build docker image
```bash
docker build -t fashion-digital-assignment:latest .
```

For connect docker image
```bash
docker ps
docker exec -it IMAGE_ID /bin/bash
```


## Tech Stack

- Scala 2.13.8
- Sbt 1.5.8
- Akka Http for routing
- Akka Stream for computing streaming data
- Alpakka for CSV parsing
- Quill for ORM and database querying
- PostgreSQL for the relational database
- Docker and docker compose for containerization
- Plugins: sbt-updates, sbt-buildinfo, sbt-assembly, sbt-scoverage, sbt-scalafmt