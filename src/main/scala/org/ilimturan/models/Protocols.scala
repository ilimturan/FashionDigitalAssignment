package org.ilimturan.models

import org.joda.time.DateTime
import spray.json.{deserializationError, DefaultJsonProtocol, JsString, JsValue, JsonFormat}

import java.util.Date

object Protocols extends DefaultJsonProtocol {

  // Joda-Time DateTime implicit converter
  implicit object JodaDateTimeJsonFormat extends JsonFormat[DateTime] {

    def write(dateTime: DateTime): JsValue = JsString(dateTime.toString)

    def read(json: JsValue): DateTime = json match {
      case JsString(s) => new DateTime(s)
      case _           => deserializationError("DateTime format wrong")
    }
  }

  // Java Date implicit converter
  implicit object JavaDateJsonFormat extends JsonFormat[Date] {

    def write(date: Date): JsValue = JsString(date.getTime.toString)

    def read(json: JsValue): Date = json match {
      case JsString(s) => new Date(s.toLong)
      case _           => deserializationError("Date format wrong")
    }
  }

  implicit val formatSpeechRequest     = jsonFormat3(SpeechRequest.apply)
  implicit val formatSpeechFileProcess = jsonFormat6(SpeechFileProcess.apply)
  implicit val formatPolitician        = jsonFormat3(Politician.apply)
  implicit val formatTopic             = jsonFormat3(Topic.apply)
  implicit val formatPoliticalSpeech   = jsonFormat5(PoliticalSpeech.apply)
}
