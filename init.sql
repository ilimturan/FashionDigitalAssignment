DROP DATABASE IF EXISTS fashion_digital_assignment;
CREATE DATABASE fashion_digital_assignment;
\connect fashion_digital_assignment

CREATE TYPE STATUS AS ENUM ('ACTIVE', 'PASSIVE');
CREATE TYPE SPEECH_PROCESS_STATUS AS ENUM ('READY', 'DOWNLOADED', 'PARSING', 'COMPLETED', 'FAILED');

CREATE TABLE speech_request(
    id BIGSERIAL NOT NULL PRIMARY KEY,
    urls TEXT NOT NULL, --all urls string
    insertTime TIMESTAMP(3) DEFAULT NOW() NOT NULL
);

CREATE TABLE speech_file_process(
    id BIGSERIAL NOT NULL PRIMARY KEY,
    requestId BIGINT REFERENCES speech_request,
    url TEXT NOT NULL,
    status SPEECH_PROCESS_STATUS NOT NULL,
    failReason TEXT,
    insertTime TIMESTAMP(3) DEFAULT NOW() NOT NULL
);

CREATE INDEX ON speech_file_process(requestId);

CREATE TABLE politician(
    id BIGSERIAL NOT NULL PRIMARY KEY,
    name TEXT NOT NULL,
    status STATUS NOT NULL
);
CREATE UNIQUE INDEX ON politician(name);

CREATE TABLE topic(
    id BIGSERIAL NOT NULL PRIMARY KEY,
    name TEXT NOT NULL,
    status STATUS NOT NULL
);
CREATE UNIQUE INDEX ON topic(name);

CREATE TABLE political_speech(
    id BIGSERIAL NOT NULL PRIMARY KEY,
    politicianId BIGINT REFERENCES politician,
    topicId BIGINT REFERENCES topic,
    wordCount INT NOT NULL,
    speechDate DATE NOT NULL
);

CREATE INDEX ON political_speech(politicianId, topicId);
CREATE INDEX ON political_speech(speechDate);
CREATE INDEX ON political_speech(topicId);

