package org.ilimturan.models

import org.ilimturan.enums.SPEECH_PROCESS_STATUS.SPEECH_PROCESS_STATUS
import org.ilimturan.enums.STATUS.STATUS
import org.joda.time.DateTime

import java.util.Date

case class SpeechRequest(
    id: Long,
    urls: String,
    insertTime: DateTime
)

case class SpeechFileProcess(
    id: Long,
    requestId: Long,
    url: String,
    status: SPEECH_PROCESS_STATUS,
    failReason: Option[String],
    insertTime: DateTime
)

case class Politician(
    id: Long,
    name: String,
    status: STATUS
)

case class Topic(
    id: Long,
    name: String,
    status: STATUS
)

case class PoliticalSpeech(
    id: Long,
    politicianId: Long,
    topicId: Long,
    wordCount: Int,
    speechDate: Date
)
