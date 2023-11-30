# FashionDigitalAssignment

Start App
> docker compose -f docker-compose.yml up -d

This command starts the PostgreSQL server and the local APP, and initializes the relational database by creating tables

Stop App
> docker compose -f docker-compose.yml down

Test
> docker compose -f docker-compose-test.yml up -d
> docker compose -f docker-compose-test.yml down

Coverage report
> sbt clean coverageReport