package org.ilimturan.models

import akka.http.scaladsl.model.ContentType
import org.apache.commons.io.input.BOMInputStream
import org.ilimturan.enums.SPEECH_PROCESS_STATUS.SPEECH_PROCESS_STATUS
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

case class PoliticalSpeech(
    id: Long,
    politicianName: String,
    topicName: String,
    wordCount: Int,
    speechDate: Date
)
//text/csv
case class TemporaryDownloadData(contentType: ContentType, inputStream: BOMInputStream)

case class PoliticParsed(
    speaker: String,
    topic: String,
    dateOfSpeech: Date,
    wordCount: Int
)
