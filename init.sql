DROP DATABASE IF EXISTS fashion_digital_assignment;
CREATE DATABASE fashion_digital_assignment;
\connect fashion_digital_assignment

CREATE TYPE STATUS AS ENUM ('ACTIVE', 'PASSIVE');
--TODO add "NO_NEED" status
CREATE TYPE SPEECH_PROCESS_STATUS AS ENUM ('READY', 'DOWNLOADED', 'PARSING', 'COMPLETED', 'FAILED');

CREATE TABLE speech_request(
    id BIGSERIAL NOT NULL PRIMARY KEY,
    urls TEXT NOT NULL, --all urls as string (raw)
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
-- TODO uniq index for "url" (duplicate download)

CREATE TABLE speech(
    id BIGSERIAL NOT NULL PRIMARY KEY,
    politicianName TEXT NOT NULL,
    topicName TEXT NOT NULL,
    wordCount INT NOT NULL,
    partitionId INT NOT NULL,
    speechDate DATE NOT NULL
);

CREATE INDEX ON speech(politicianName, topicName);
CREATE INDEX ON speech(speechDate);
CREATE INDEX ON speech(topicName);


--------- AGG -------
CREATE TABLE agg_politician_speech_count(
    id BIGSERIAL NOT NULL PRIMARY KEY,
    politicianName TEXT NOT NULL,
    partitionId INT NOT NULL,
    aggCount BIGINT NOT NULL,
    aggTime TIMESTAMP(3) DEFAULT NOW() NOT NULL
);
CREATE INDEX ON agg_politician_speech_count(aggTime);

CREATE TABLE agg_politician_speech_topic_count(
    id BIGSERIAL NOT NULL PRIMARY KEY,
    politicianName TEXT NOT NULL,
    topicName TEXT NOT NULL,
    aggCount BIGINT NOT NULL,
    aggTime TIMESTAMP(3) DEFAULT NOW() NOT NULL
);
CREATE INDEX ON agg_politician_speech_topic_count(topicName, aggTime);

CREATE TABLE agg_politician_word_count(
    id BIGSERIAL NOT NULL PRIMARY KEY,
    politicianName TEXT NOT NULL,
    aggCount BIGINT NOT NULL,
    aggTime TIMESTAMP(3) DEFAULT NOW() NOT NULL
);
CREATE INDEX ON agg_politician_word_count(politicianName, aggTime);

