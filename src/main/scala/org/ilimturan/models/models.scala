package org.ilimturan.models

import org.ilimturan.enums.SPEECH_PROCESS_STATUS.SPEECH_PROCESS_STATUS
import org.ilimturan.enums.STATUS.STATUS
import org.joda.time.DateTime

import java.util.Date

case class SpeechRequest(
    id: Option[Long],
    urls: String,
    insertTime: DateTime
)

case class SpeechFileProcess(
    id: Option[Long],
    requestId: Long,
    url: String,
    status: SPEECH_PROCESS_STATUS,
    failReason: Option[String],
    insertTime: DateTime
)

case class Politician(
    id: Option[Long],
    name: String,
    status: STATUS
)

case class Topic(
    id: Option[Long],
    name: String,
    status: STATUS
)

case class PoliticalSpeech(
    id: Option[Long],
    politicianId: Long,
    topicId: Long,
    wordCount: Int,
    speechDate: Date
)
