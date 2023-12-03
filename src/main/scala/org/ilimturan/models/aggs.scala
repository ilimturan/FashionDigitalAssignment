package org.ilimturan.models

case class AggPoliticianSpeechCount(
    id: Long,
    politicianName: String,
    partitionId: Int, //year id
    aggCount: Long
    //aggTime: Timestamp
)

case class AggPoliticianSpeechTopicCount(
    id: Long,
    politicianName: String,
    topicName: String,
    aggCount: Long
    //aggTime: Timestamp
)

case class AggPoliticianSpeechWordCount(
    id: Long,
    politicianName: String,
    aggCount: Long
)
