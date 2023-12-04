FROM hseeberger/scala-sbt:eclipse-temurin-11.0.14.1_1.6.2_3.1.1
WORKDIR /app
COPY . /app
RUN sbt clean assembly