# FashionDigitalAssignment

This repository contains multiple services within a single codebase,
provide to a simple stream/flow pipeline (producer, consumer, aggregator, and query service).

## Start App

### Step 1

To begin, start PostgreSQL locally:

```bash
docker-compose -f docker-compose.yml up -d
```

This command initiates a PostgreSQL server and sets up the relational database by creating tables.

### Step 2

The next step is to start the applications:

```bash
sbt "run-main org.ilimturan.ProducerSpeech"
```
```bash
sbt "run-main org.ilimturan.ConsumerSpeech"
```

### Stop Docker Compose

To stop the docker compose, use the following command:

```bash
docker-compose -f docker-compose.yml down
```

## Tests

### Step 1

For integration tests (no need for unit tests), first, run:

```bash
docker-compose -f docker-compose-test.yml up -d
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
docker-compose -f docker-compose-test.yml down
```

## Code Formatting

For code formatting:

```bash
sbt scalafmt
```

```bash
sbt test:scalafmt
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