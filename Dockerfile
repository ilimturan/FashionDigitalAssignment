FROM mozilla/sbt:latest
ENV SBT_VERSION 1.5.8
WORKDIR /fsh
COPY . /fsh
RUN sbt assembly